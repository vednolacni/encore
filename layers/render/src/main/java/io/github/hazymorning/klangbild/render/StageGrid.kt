package io.github.hazymorning.klangbild.render


/**
 * The stage's grid on a 960 × 540 dp screen, laid out as a music player: the cover (290 dp) high
 * on the left, as far from the top edge as the buttons stand from the bottom edge, the text column
 * beside it, and below them the progress bar across the screen from margin to margin with the
 * buttons under its right end. While the song's words take the column, the song's title and
 * artist stand under the cover.
 *
 * Two quiet lines frame the column within the artwork's straight edge beside it, a corner radius
 * in from its top and bottom edges: the status line (time, where the music plays) hangs there
 * with its cap height, and what comes next ends there with its descenders. Every line in the
 * column starts on one left edge.
 *
 *  ┌──────────────────────────────────────────────────────────┐
 *  │        ╭───────────╮      21:47 · Everywhere              │  ← 44 dp from the top
 *  │        │  artwork  │      words, the line sung now         │
 *  │        ╰───────────╯      in the middle                    │
 *  │        Title                                               │
 *  │        Artist                                              │
 *  │        ━━━━━━━━━━━━━━━━━━━━━━━━━━──────────────────────     │
 *  │        1:35                                     4:11       │
 *  │                                   [ Lyrics On ][ Settings ]│  ← 44 dp from the bottom
 *  └──────────────────────────────────────────────────────────┘
 *
 * The screens without a song, the setup page and the settings menu, stand on a grid of their own
 * ([centered]): a square of [CENTERED_ART] in the very middle of the height (the setup's code), its
 * words beside it, as there is no song under it, no bar and no button.
 */
data class StageGrid(val screen: ScreenSpec, val centered: Boolean = false) {
    private val sx = screen.widthDp / GRID_WIDTH
    private val sy = screen.heightDp / GRID_HEIGHT

    /** Left margin before the artwork, and after the column and the bar on the right. */
    val margin = MARGIN * sx
    val rightMargin = MARGIN * sx

    /** Side of the artwork's square. */
    val art = (if (centered) CENTERED_ART else ART) * sy

    /** The artwork's top edge: as far from the top as the buttons from the bottom ([centered]: in the middle). */
    val artTop = if (centered) (screen.heightDp - art) / 2f else EDGE * sy
    val artRect: RectDp get() = RectDp(margin, artTop, art, art)

    /** Space between artwork and text. */
    val gap = ART_TEXT_GAP * sx
    val columnLeft get() = margin + art + gap
    val columnWidth get() = screen.widthDp - rightMargin - columnLeft

    /** Center line of the text block (from its first cap height to its last baseline). */
    val textCenter get() = artTop + art / 2f

    /** Where the status line's cap height hangs: a corner radius below the artwork's top edge. */
    val statusTop get() = artTop + radius

    /** Where the "Up next" line's descenders end: a corner radius above the artwork's bottom edge. */
    val footerBottom get() = artTop + art - radius

    /** Corner radius of a square artwork (dp): a hair over 2 % of its side. */
    val radius get() = RADIUS * art / RADIUS_ART

    /** Where the song's title under the cover has its cap height (dp). */
    val captionTop get() = artTop + art + CAPTION_GAP * sy

    /** The progress bar's left end and its width (dp), across the screen from margin to margin. */
    val barLeft get() = margin
    val barWidth get() = screen.widthDp - margin - rightMargin

    /** The buttons' row, its lower edge as far from the bottom as the artwork from the top (dp). */
    val buttonsBottom get() = screen.heightDp - EDGE * sy
    val buttonsTop get() = buttonsBottom - BUTTON_HEIGHT * sy

    /** The baseline of the progress bar's times, above the buttons (dp). */
    val timesBaseline get() = buttonsTop - TIMES_GAP * sy

    companion object {
        /** The grid's size (dp): every measure here is for a screen this big, scaled to the real one. */
        const val GRID_WIDTH = 960f
        const val GRID_HEIGHT = 540f

        /** Left and right margins (dp at 960 × 540). */
        const val MARGIN = 88f

        /** The artwork's side (dp at 960 × 540). */
        const val ART = 290f

        /** The square of the screens without a song ([centered], dp at 960 × 540). */
        const val CENTERED_ART = 368f

        /** Between the artwork and the text (dp at 960 × 540). */
        const val ART_TEXT_GAP = 64f

        /** The artwork's room above it and the buttons' room below them (dp at 960 × 540). */
        const val EDGE = 44f

        /** A button's height (dp at 960 × 540). */
        const val BUTTON_HEIGHT = 36f

        /** From the artwork's bottom edge to the title's cap height under it (dp at 960 × 540). */
        const val CAPTION_GAP = 22f

        /** From the times' baseline down to the buttons (dp at 960 × 540). */
        const val TIMES_GAP = 14f

        /** Corner radius of square artwork: [RADIUS] dp on a cover [RADIUS_ART] dp wide, scaled with it. */
        const val RADIUS = 8f
        const val RADIUS_ART = 368f
    }
}
