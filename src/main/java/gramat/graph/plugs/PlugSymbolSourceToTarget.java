package gramat.graph.plugs;

import gramat.actions.ActionStore;
import gramat.badges.Badge;
import gramat.badges.BadgeMode;
import gramat.graph.Graph;
import gramat.graph.Link;
import gramat.graph.LinkSymbol;
import gramat.graph.Node;

public class PlugSymbolSourceToTarget extends PlugSymbol {

    public PlugSymbolSourceToTarget(LinkSymbol link) {
        super(link.symbol, link.beforeActions, link.afterActions);
    }

    @Override
    public Link connectTo(Graph graph, Node newSource, Node newTarget, Badge newBadge, ActionStore wrappingBefore, ActionStore wrappingAfter) {
        return graph.createLink(
                newSource, newTarget,
                ActionStore.join(wrappingBefore, beforeActions),
                ActionStore.join(afterActions, wrappingAfter),
                symbol, newBadge, BadgeMode.NONE);
    }

}