package io.github.vednolacni.encore.ui.stage

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import io.github.vednolacni.encore.color.Appearance
import io.github.vednolacni.encore.color.Lamp
import io.github.vednolacni.encore.domain.PlayerStatus
import io.github.vednolacni.encore.domain.TimeText
import io.github.vednolacni.encore.render.ArtworkScene
import io.github.vednolacni.encore.render.StageGrid
import io.github.vednolacni.encore.ui.Motion
import io.github.vednolacni.encore.ui.theme.Face
import io.github.vednolacni.encore.ui.theme.Fonts
import io.github.vednolacni.encore.ui.theme.rememberFigures
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * The wall under everything: black in the dark (on an OLED the pixels are off), paper in
 * the light. It reaches [BEYOND_EDGES_DP] beyond the screen on every side, so the screen's drift
 * against burn-in never uncovers an edge. [beyondOnly] draws only that margin (a new wall's
 * color coming up with its light during a change); [alpha] fades it.
 */
@Composable
fun BaseWall(color: Color, modifier: Modifier = Modifier, alpha: () -> Float = { 1f }, beyondOnly: Boolean = false) {
    Canvas(modifier.fillMaxSize()) {
        val a = alpha().coerceIn(0f, 1f)
        if (a <= 0.002f) return@Canvas
        val m = BEYOND_EDGES_DP * density
        val w = size.width
        val h = size.height
        if (!beyondOnly) {
            drawRect(color, Offset(-m, -m), Size(w + 2 * m, h + 2 * m), alpha = a)
        } else {
            drawRect(color, Offset(-m, -m), Size(m, h + 2 * m), alpha = a)
            drawRect(color, Offset(w, -m), Size(m, h + 2 * m), alpha = a)
            drawRect(color, Offset(0f, -m), Size(w, m), alpha = a)
            drawRect(color, Offset(0f, h), Size(w, m), alpha = a)
        }
    }
}

/** How far every full-screen ground reaches beyond the screen (dp): more than the drift (±6 dp) ever moves it. */
const val BEYOND_EDGES_DP = 24f

/**
 * The wall around the artwork, one static bitmap scaled softly to the screen. In the dark it is
 * the artwork's light ([io.github.vednolacni.encore.render.WallGlow]), and [level] is its
 * brightness in linear light (pause, the Intensity setting: 0 = off, 1 = as designed,
 * 1.45 = strong). In the light it is plain paper. [alpha] fades the layer during a change.
 */
@Composable
fun WallLayer(
    scene: ArtworkScene,
    modifier: Modifier = Modifier,
    alpha: () -> Float = { 1f },
    level: () -> Float = { 1f },
) {
    // With the cover colors background, the cover's own colors are the wall.
    if (scene.coverField) return FieldLayer(scene, modifier, alpha, level)
    val image = remember(scene) { scene.wall.asImageBitmap() }
    val filters = remember { HashMap<Int, ColorFilter?>() }
    val lit = scene.appearance == Appearance.DARK
    Canvas(modifier.fillMaxSize()) {
        val a = alpha().coerceIn(0f, 1f)
        val l = if (lit) level().coerceIn(0f, 2f) else 1f
        if (a <= 0.002f || l <= 0.002f) return@Canvas
        // The bitmap is sRGB: a factor k in linear light is k^(1/2.2) on its values.
        val step = (l.pow(1f / 2.2f) * 40f).roundToInt()
        val filter = filters.getOrPut(step) { if (step == 40) null else scaleFilter(step / 40f) }
        drawImage(
            image,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            alpha = a,
            colorFilter = filter,
            filterQuality = FilterQuality.Low,
        )
    }
}

private fun scaleFilter(k: Float) = ColorFilter.colorMatrix(
    ColorMatrix(floatArrayOf(k, 0f, 0f, 0f, 0f, 0f, k, 0f, 0f, 0f, 0f, 0f, k, 0f, 0f, 0f, 0f, 0f, 1f, 0f)),
)

/**
 * The artwork, drawn 1:1 at a whole-pixel position (the bitmap already has its exact display
 * size) with softly rounded corners. [dim] (pause) darkens it through the color matrix, not
 * through transparency. Nothing is drawn around it: no frame, no line, no shadow.
 */
@Composable
fun ArtworkLayer(
    scene: ArtworkScene,
    modifier: Modifier = Modifier,
    alpha: () -> Float = { 1f },
    dim: () -> Float = { 1f },
) {
    val shader = remember(scene) {
        android.graphics.BitmapShader(scene.cover, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP)
    }
    val brush = remember(shader) { ShaderBrush(shader) }
    val place = remember { android.graphics.Matrix() }
    val filters = remember(scene) { HashMap<Int, ColorFilter>() }
    val rect = scene.layout.artRect
    Canvas(
        modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha().coerceIn(0f, 1f) },
    ) {
        val step = (dim().coerceIn(0f, 1f) * 50f).roundToInt()
        val filter = filters.getOrPut(step) { coverFilter(step / 50f, scene.appearance) }
        val x = (rect.left * density).roundToInt().toFloat()
        val y = (rect.top * density).roundToInt().toFloat()
        val w = scene.cover.width.toFloat()
        val h = scene.cover.height.toFloat()
        val corner = CornerRadius(scene.corner * density)
        place.setTranslate(x, y)
        shader.setLocalMatrix(place)
        drawRoundRect(brush, Offset(x, y), Size(w, h), corner, colorFilter = filter)
    }
}

/**
 * Color matrix for the artwork: the [Lamp]'s breath of warmth, [dim] (a pause) and a gentle
 * highlight compression, so even a white cover never glares on black. White becomes a soft paper
 * tone around L* 88 instead of #FFFFFF; a full-screen-bright print would also make the TV's
 * brightness limiter dim everything. On the light wall a white cover stays a little whiter
 * than the paper around it.
 */
fun coverFilter(dim: Float = 1f, appearance: Appearance = Appearance.DARK): ColorFilter {
    val (r, g, b) = coverScale(dim, appearance)
    return ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                r, 0f, 0f, 0f, 0f,
                0f, g, 0f, 0f, 0f,
                0f, 0f, b, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
}

/** The per-channel scale of [coverFilter] (red, green, blue). */
fun coverScale(dim: Float = 1f, appearance: Appearance = Appearance.DARK): Triple<Float, Float, Float> {
    val k = dim * if (appearance == Appearance.LIGHT) 1.06f else 1f
    val w = Lamp.WARMTH
    return Triple(k * 0.92f * (1f + 0.0375f * w), k * 0.905f * (1f - 0.0066f * w), k * 0.876f * (1f - 0.045f * w))
}


/**
 * The text block beside the artwork, centered on it from the first line's cap height to the
 * last baseline: (work), title, (version), artist, album · year, then the progress bar (or the
 * station, or a notice), the bar standing across the screen under the artwork. What comes next
 * sits apart on the artwork's bottom edge. [alpha] and [rise] animate a change line by line;
 * [dim] darkens the text while paused; [shift] moves the column to the right of its place in the
 * grid (dp). While the song's words take the column ([StageModel.lyrics]), they stand there
 * instead ([LyricsText]).
 */
@Composable
internal fun TextLayer(
    model: StageModel,
    modifier: Modifier = Modifier,
    plan: TextPlan = rememberTextPlan(model),
    alpha: (TextLine) -> Float = { 1f },
    rise: (TextLine) -> Float = { 0f },
    dim: () -> Float = { 1f },
    shift: () -> Float = { 0f },
) {
    val lyrics = model.lyrics
    if (lyrics != null) {
        LyricsText(model, lyrics, modifier, alpha, rise, dim, shift)
        return
    }
    val layout = model.scene.layout
    val t = model.text
    val styles = remember(model.scene.palette) { StageStyles(model.scene.palette) }
    val block = plan.styles
    val localDensity = LocalDensity.current
    val colW = (layout.columnWidth * localDensity.density).roundToInt()
    val playing = model.status == PlayerStatus.PLAYING || model.status == PlayerStatus.BUFFERING

    fun line(which: TextLine): Modifier = Modifier.lineMotion(which, alpha, rise)
    val bottomLine = model.line
    val measurer = rememberTextMeasurer(cacheSize = 4)

    Layout(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = dim().coerceIn(0f, 1f) },
        content = {
            // 0: work line
            val work = t.work
            if (work != null) BasicText(work, line(TextLine.WORK), style = block.work, maxLines = 2, overflow = TextOverflow.Ellipsis) else Box(Modifier)
            // 1: title
            BasicText(plan.title, line(TextLine.TITLE), style = plan.titleStyle, maxLines = plan.titleLines, overflow = TextOverflow.Ellipsis)
            // 2: version
            if (plan.version != null) BasicText(plan.version, line(TextLine.VERSION), style = plan.versionStyle, maxLines = 1, overflow = TextOverflow.Ellipsis) else Box(Modifier)
            // 3: artist
            if (plan.artists != null) {
                BasicText(plan.artists, line(TextLine.ARTIST), style = plan.artistStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            } else {
                Box(Modifier)
            }
            // 4: album · year, or the chapter, which changes in place
            when {
                model.chapter != null -> Crossfade(
                    targetState = model.chapter,
                    modifier = line(TextLine.ALBUM),
                    animationSpec = tween(Motion.LINE_FADE_MS, easing = Motion.Dimmer),
                    label = "chapter",
                ) { chapter ->
                    // A long name ends after a whole word, never inside one.
                    val fitted = remember(chapter, colW, block.album) { fitWords(measurer, chapter, block.album, colW) }
                    BasicText(fitted, style = block.album, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                plan.album != null -> BasicText(plan.album, line(TextLine.ALBUM), style = block.album, maxLines = 1, overflow = TextOverflow.Ellipsis)
                else -> Box(Modifier)
            }
            // 5: progress bar, station or notice; changes cross-fade
            BottomLine(bottomOf(model), model, styles, playing, line(TextLine.BOTTOM), label = "bottom-line")
            // 6: what comes next, on the artwork's bottom edge
            NextLine(plan.next, plan.nextLead, styles, line(TextLine.NEXT))
        },
    ) { measurables, constraints ->
        val c = Constraints(maxWidth = colW)
        val work = measurables[0].measure(c)
        val title = measurables[1].measure(c)
        val version = measurables[2].measure(c)
        val artist = measurables[3].measure(c)
        val album = measurables[4].measure(c)
        // The bar (or a line in its place) stands across the screen.
        val barW = (layout.barWidth * density).roundToInt()
        val bottom = measurables[5].measure(Constraints(minWidth = barW, maxWidth = barW))
        val nextLine = measurables[6].measure(c)
        fun px(dp: Float) = (dp * density).roundToInt()
        fun capOf(style: TextStyle) = (style.fontSize.toPx() * Fonts.CAP_HEIGHT).roundToInt()

        // Each part: where its cap height is (from its top), where its last baseline is, and
        // its optical indent.
        class Part(val p: Placeable, val capTop: Int, val baseline: Int, val gap: Int, val dx: Int = 0)
        val parts = ArrayList<Part>()
        fun text(p: Placeable, style: TextStyle, gap: Float) {
            if (p.height == 0) return
            val first = p[FirstBaseline].takeIf { it != AlignmentLine.Unspecified } ?: p.height
            val last = p[LastBaseline].takeIf { it != AlignmentLine.Unspecified } ?: p.height
            parts += Part(p, first - capOf(style), last, if (parts.isEmpty()) 0 else px(gap), px(opticalIndent(style.fontSize.value)))
        }
        text(work, block.work, 0f)
        text(title, plan.titleStyle, Gaps.WORK)
        text(version, plan.versionStyle, Gaps.VERSION)
        text(artist, plan.artistStyle, Gaps.ARTIST)
        text(album, block.album, Gaps.ALBUM)
        // Positions of each cap height, relative to the first one.
        var y = 0
        val capY = IntArray(parts.size)
        parts.forEachIndexed { i, part ->
            if (i > 0) {
                val prev = parts[i - 1]
                y = capY[i - 1] - prev.capTop + prev.baseline + part.gap
            }
            capY[i] = y
        }
        val blockH = parts.lastOrNull()?.let { capY.last() - it.capTop + it.baseline } ?: 0
        val top = (layout.textCenter * density).roundToInt() - blockH / 2
        // The last line's descenders end where the column ends.
        val footer = (layout.footerBottom * density - styles.next.fontSize.toPx() * Fonts.DESCENT).roundToInt()
        val barLeft = (layout.barLeft * density).roundToInt()
        val across = acrossTop(bottom, bottomLine != null, layout, styles, this)
        layout(constraints.maxWidth, constraints.maxHeight) {
            // Read while placing: a column that glides is only placed anew.
            val x = ((layout.columnLeft + shift()) * density).roundToInt()
            parts.forEachIndexed { i, part -> part.p.place(x + part.dx, top + capY[i] - part.capTop) }
            if (bottom.height > 0) bottom.place(barLeft, across)
            if (nextLine.height > 0) {
                val b = nextLine[FirstBaseline].takeIf { it != AlignmentLine.Unspecified } ?: nextLine.height
                nextLine.place(x, footer - b)
            }
        }
    }
}


/**
 * The top (px) of what stands in the progress bar's place across the screen
 * ([p], measured as wide as the bar). The bar's block ends at its times' baseline, which stands
 * on [StageGrid.timesBaseline]; a line of words in its place has its cap height where the bar's
 * top would be.
 */
internal fun acrossTop(p: Placeable, isText: Boolean, layout: StageGrid, styles: StageStyles, d: Density): Int {
    val times = (layout.timesBaseline * d.density).roundToInt()
    if (!isText) return times - p.height
    val cap = with(d) { (styles.label.fontSize.toPx() * Fonts.CAP_HEIGHT).roundToInt() }
    val barTop = times - with(d) { ((BAR_THICKNESS + BAR_LABEL_GAP) * density).roundToInt() + (styles.time.fontSize.toPx() * Fonts.CAP_HEIGHT).roundToInt() }
    val first = p[FirstBaseline].takeIf { it != AlignmentLine.Unspecified } ?: p.height
    return barTop + cap - first
}

/**
 * What comes next after its [lead] ("Up next"); a new one (the next chapter, while a book plays
 * on) cross-fades in place, and so does one that comes or
 * goes ([text] null: none).
 */
@Composable
internal fun NextLine(text: String?, lead: String, styles: StageStyles, modifier: Modifier) {
    Crossfade(text?.let { lead to it }, modifier, tween(Motion.LINE_FADE_MS, easing = Motion.Dimmer), label = "next") { line ->
        if (line == null) {
            Box(Modifier)
        } else {
            BasicText(nextText(line.second, styles, line.first), style = styles.next, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/**
 * The status line at the top of the column: the time, the values of one's own, then where the
 * music plays and what it is doing ("Living room · Paused", or a passing volume). Its cap
 * height is on the artwork's top edge, and it starts on the column's left edge like everything
 * else. The place has its room first; values that do not fit beside it are left out, from the
 * last, never cut. The place cross-fades when it changes; the clock and the values just tick,
 * the clock's figures in fixed cells. [shift] moves it with the column (dp).
 */
@Composable
fun StatusLayer(
    model: StageModel,
    modifier: Modifier = Modifier,
    shift: () -> Float = { 0f },
    /** How strongly it shows: the status line of the other wall cross-fades with the stage. */
    alpha: (() -> Float)? = null,
) {
    val status = model.statusLine
    if (status == null || status.isEmpty) return
    val layout = model.scene.layout
    val styles = remember(model.scene.palette) { StageStyles(model.scene.palette) }
    val figures = rememberFigures(styles.time, Face.TITLE)
    val clockText = status.clock
    val measurer = rememberTextMeasurer()
    val local = LocalDensity.current
    val separator = TimeText.SEPARATOR.trimStart()
    // Which values fit beside the clock and the place, worked out on their measured widths.
    val values = remember(status, styles, layout, local) {
        fun width(text: AnnotatedString, style: TextStyle) = measurer.measure(text, style, maxLines = 1, softWrap = false).size.width
        val space = with(local) { styles.label.fontSize.toPx() } * STATUS_SPACE_EM
        var room = layout.columnWidth * local.density
        if (clockText != null) room -= width(figures.of(clockText), styles.time) + space
        status.lead?.let { room -= width(AnnotatedString(separator + it), styles.label) }
        val fit = ArrayList<String>()
        for ((i, v) in status.values.withIndex()) {
            val need = width(AnnotatedString(if (clockText == null && i == 0) v else separator + v), styles.label) + space
            if (need > room) break
            fit += v
            room -= need
        }
        fit
    }
    val leadAfter = clockText != null || values.isNotEmpty()
    val faded = if (alpha == null) modifier else modifier.graphicsLayer {
        this.alpha = alpha().coerceIn(0f, 1f)
        // Faded draw by draw: its parts never overlap, and no layer of the screen's size is needed.
        compositingStrategy = CompositingStrategy.ModulateAlpha
    }
    Layout(
        modifier = faded.fillMaxSize(),
        content = {
            if (clockText != null) BasicText(figures.of(clockText), style = styles.time.copy(color = styles.muted), maxLines = 1) else Box(Modifier)
            values.forEachIndexed { i, v ->
                BasicText(if (clockText == null && i == 0) v else separator + v, style = styles.label, maxLines = 1)
            }
            Crossfade(
                targetState = status.lead,
                animationSpec = tween(Motion.LINE_FADE_MS, easing = Motion.Dimmer),
                label = "status-lead",
            ) { lead ->
                if (lead != null) {
                    BasicText(
                        if (leadAfter) separator + lead else lead,
                        style = styles.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    Box(Modifier)
                }
            }
        },
    ) { measurables, constraints ->
        val space = (styles.label.fontSize.toPx() * STATUS_SPACE_EM).roundToInt()
        val column = (layout.columnWidth * density).roundToInt()
        val clock = measurables.first().measure(Constraints())
        val parts = measurables.subList(1, measurables.size - 1).map { it.measure(Constraints()) }
        // Everything before the place, with the space after each part.
        val before = listOf(clock).filter { it.width > 0 } + parts
        val used = before.sumOf { it.width + space }
        val lead = measurables.last().measure(Constraints(maxWidth = (column - used).coerceAtLeast(0)))
        fun Placeable.first(): Int = this[FirstBaseline].takeIf { it != AlignmentLine.Unspecified } ?: height
        // Cap height where the column begins.
        val baseline = (layout.statusTop * density).roundToInt() + (styles.label.fontSize.toPx() * Fonts.CAP_HEIGHT).roundToInt()
        layout(constraints.maxWidth, constraints.maxHeight) {
            var x = ((layout.columnLeft + shift()) * density).roundToInt()
            for (p in before) {
                p.place(x, baseline - p.first())
                x += p.width + space
            }
            if (lead.height > 0) lead.place(x, baseline - lead.first())
        }
    }
}

/** The space between the parts of the status line: a word space of its labels. */
private const val STATUS_SPACE_EM = 0.26f
