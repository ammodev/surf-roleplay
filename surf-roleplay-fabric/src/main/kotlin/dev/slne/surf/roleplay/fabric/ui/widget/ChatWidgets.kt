package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.AvatarSize
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.ScrollOrientation
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TextKind

/**
 * A chat view: its messages, stacked in a scrolling column. It starts at the newest message and
 * stays there when messages are appended, unless the player scrolled up.
 *
 * @param id the id of the widget
 */
class ChatViewWidget(id: String) : ScrollAreaWidget(id, ScrollOrientation.VERTICAL) {
    init {
        stickToBottom = true
        gap = GAP
        padding = Insets(PADDING, PADDING, PADDING, PADDING)
    }

    /**
     * Holds the chat view metrics.
     */
    companion object {
        /**
         * The space between two messages.
         */
        const val GAP: Int = 6

        /**
         * The space inside the view's edges.
         */
        const val PADDING: Int = 4
    }
}

/**
 * A message of a chat view, as wide as the view: the sender's avatar for messages of others, then
 * a column of the name and time above the bubble. The player's own messages sit at the right.
 *
 * @param id the id of the widget
 * @property own whether the message is the player's own
 */
class ChatMessageWidget(id: String, val own: Boolean) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        gap = GAP
        mainAlign = if (own) Align.END else Align.START
        crossAlign = Align.START
        width = Sizing.grow()
    }

    /**
     * The bubble holding the content of the message.
     */
    val bubble: ChatBubbleWidget? get() = WidgetTree.find(this, "$id:bubble") as? ChatBubbleWidget

    /**
     * Holds the message metrics and the way it is put together.
     */
    companion object {
        /**
         * The space between the avatar and the rest.
         */
        const val GAP: Int = 6

        /**
         * Builds a message: the avatar for messages of others, then the name and time above a
         * bubble with the content.
         *
         * @param id the id of the message
         * @param own whether the message is the player's own
         * @param avatar the avatar of the sender, used only for messages of others
         * @param name the sender's name as component JSON, empty for none
         * @param time the time of the message as component JSON, empty for none
         * @param content the widgets of the content
         * @return the message
         */
        fun build(id: String, own: Boolean, avatar: AvatarWidget, name: String, time: String, content: List<Widget>): ChatMessageWidget =
            ChatMessageWidget(id, own).apply {
                if (!own) childList += avatar
                val column = ContainerWidget("$id:column", Axis.VERTICAL).apply {
                    gap = COLUMN_GAP
                    crossAlign = if (own) Align.END else Align.START
                }
                if (name.isNotEmpty() || time.isNotEmpty()) {
                    column.childList += ContainerWidget("$id:header", Axis.HORIZONTAL).apply {
                        gap = HEADER_GAP
                        crossAlign = Align.CENTER
                        if (name.isNotEmpty()) childList += TextWidget("$id:name", TextKind.SMALL, name)
                        if (time.isNotEmpty()) childList += TextWidget("$id:time", TextKind.MUTED, time)
                    }
                }
                column.childList += ChatBubbleWidget("$id:bubble", own).apply { childList += content }
                childList += column
            }

        /**
         * The space between the header and the bubble.
         */
        private const val COLUMN_GAP: Int = 2

        /**
         * The space between the name and the time.
         */
        private const val HEADER_GAP: Int = 4

        /**
         * The size of a sender's avatar.
         */
        val AVATAR_SIZE: AvatarSize = AvatarSize.SM
    }
}

/**
 * The bubble of a chat message: its content, stacked, on the primary colour for the player's own
 * messages and on the muted colour for those of others, with the texts in the matching colour.
 *
 * @param id the id of the widget
 * @property own whether the message is the player's own
 */
class ChatBubbleWidget(id: String, val own: Boolean) : ContainerWidget(id, Axis.VERTICAL) {
    init {
        gap = GAP
        padding = Insets(PADDING_Y, PADDING_X, PADDING_Y, PADDING_X)
    }

    /**
     * Draws the bubble, tints the texts inside it, then draws them.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        ui.fillRounded(bounds, if (own) tokens.primary else tokens.muted, RADIUS)
        val text = if (own) tokens.primaryForeground else tokens.foreground
        WidgetTree.visit(this) { if (it is TextWidget) it.tint = text }
        super.render(ui, context, mouseX, mouseY)
    }

    /**
     * Holds the bubble metrics.
     */
    companion object {
        /**
         * The space between two parts of the content.
         */
        const val GAP: Int = 2

        /**
         * The space left and right inside the bubble.
         */
        const val PADDING_X: Int = 6

        /**
         * The space above and below inside the bubble.
         */
        const val PADDING_Y: Int = 4

        /**
         * The corner radius of the bubble.
         */
        const val RADIUS: Int = 4
    }
}
