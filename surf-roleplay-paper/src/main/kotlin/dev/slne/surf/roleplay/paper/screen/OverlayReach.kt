package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogContentElement
import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogElement
import dev.slne.surf.roleplay.api.client.common.screen.ContainerElement
import dev.slne.surf.roleplay.api.client.common.screen.ContextMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogContentElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogElement
import dev.slne.surf.roleplay.api.client.common.screen.DrawerContentElement
import dev.slne.surf.roleplay.api.client.common.screen.DrawerElement
import dev.slne.surf.roleplay.api.client.common.screen.DropdownMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.HoverCardContentElement
import dev.slne.surf.roleplay.api.client.common.screen.HoverCardElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuContentElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSubElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSubTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.MenubarMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.MenubarTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.PopoverContentElement
import dev.slne.surf.roleplay.api.client.common.screen.PopoverElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetContentElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetElement

/**
 * Answers whether the player can reach the elements of a server-held screen through its
 * overlays.
 *
 * An overlay host is reachable when it is not inside the content of an unreachable host and the
 * server holds it as open, it opens without a trigger widget (a context menu or a hover card), or
 * one of its triggers holds an enabled widget that opens it with every element between that widget
 * and the host enabled. An element is reachable when every host whose content it is inside is
 * reachable.
 *
 * @param tree the server's copy of the screen
 */
class OverlayReach(tree: ServerScreenTree) {

    /**
     * The parent of every element but the root, by element id.
     */
    private val parents = HashMap<String, ScreenElement>().also { map ->
        fun walk(element: ScreenElement) {
            if (element is ContainerElement) element.children.forEach { child -> map[child.id] = element; walk(child) }
        }
        walk(tree.root)
    }

    /**
     * Returns whether the player can reach an element: whether every overlay whose content holds
     * it is reachable.
     *
     * @param element the element
     * @return whether the element is reachable
     */
    fun isReachable(element: ScreenElement): Boolean {
        var current = element
        while (true) {
            val parent = parents[current.id] ?: return true
            if (isHost(parent) && isContent(current)) return isHostReachable(parent)
            current = parent
        }
    }

    /**
     * Returns whether the player can open an overlay host themselves: whether it is reachable
     * and opens without a trigger widget or through an enabled one.
     *
     * @param host the host
     * @return whether the player can open it
     */
    fun canOpen(host: ScreenElement): Boolean = isReachable(host) && (opensWithoutTrigger(host) || hasUsableTrigger(host))

    /**
     * Returns whether an overlay host is reachable.
     *
     * @param host the host
     * @return whether it is reachable
     */
    private fun isHostReachable(host: ScreenElement): Boolean =
        isReachable(host) && (isHeldOpen(host) || opensWithoutTrigger(host) || hasUsableTrigger(host))

    /**
     * Returns whether one of a host's triggers holds an enabled widget that opens it, with every
     * element between that widget and the host enabled.
     *
     * @param host the host
     * @return whether it has a usable trigger
     */
    private fun hasUsableTrigger(host: ScreenElement): Boolean =
        (host as ContainerElement).children.filterNot(::isContent).any(::holdsOpener)

    /**
     * Returns whether an enabled subtree holds an enabled widget that opens an overlay.
     *
     * @param element the root of the subtree
     * @return whether it does
     */
    private fun holdsOpener(element: ScreenElement): Boolean {
        if (!ElementRules.isEnabled(element)) return false
        if (isOpener(element)) return true
        return element is ContainerElement && element.children.any(::holdsOpener)
    }

    /**
     * Returns whether the server holds a host as open.
     *
     * @param host the host
     * @return whether it is open on the server
     */
    private fun isHeldOpen(host: ScreenElement): Boolean = ElementRules.input(host)?.current?.invoke(host) == "true"

    /**
     * Holds which elements are hosts, contents and openers.
     */
    companion object {
        /**
         * Returns whether an element is an overlay host.
         *
         * @param element the element
         * @return whether it is one
         */
        fun isHost(element: ScreenElement): Boolean = when (element) {
            is PopoverElement, is HoverCardElement, is DropdownMenuElement, is MenuSubElement, is ContextMenuElement,
            is MenubarMenuElement, is DialogElement, is AlertDialogElement, is SheetElement, is DrawerElement,
            -> true
            else -> false
        }

        /**
         * Returns whether an element is the content of an overlay host.
         *
         * @param element the element
         * @return whether it is one
         */
        private fun isContent(element: ScreenElement): Boolean = when (element) {
            is PopoverContentElement, is HoverCardContentElement, is MenuContentElement, is DialogContentElement,
            is AlertDialogContentElement, is SheetContentElement, is DrawerContentElement,
            -> true
            else -> false
        }

        /**
         * Returns whether a host opens without a trigger widget: on a right click or on hover
         * over its area.
         *
         * @param host the host
         * @return whether it does
         */
        private fun opensWithoutTrigger(host: ScreenElement): Boolean = host is ContextMenuElement || host is HoverCardElement

        /**
         * Returns whether a widget opens the overlay whose trigger it is part of: a widget with
         * an action, or a sub-menu or menubar trigger.
         *
         * @param element the widget
         * @return whether it does
         */
        private fun isOpener(element: ScreenElement): Boolean =
            element is MenuSubTriggerElement || element is MenubarTriggerElement || ElementRules.rule(element)?.action?.invoke(element) != null
    }
}
