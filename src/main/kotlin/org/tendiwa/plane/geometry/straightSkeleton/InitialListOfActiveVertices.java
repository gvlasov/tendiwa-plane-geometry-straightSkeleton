package org.tendiwa.plane.geometry.straightSkeleton;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import kotlin.Triple;
import org.tendiwa.collections.IterablesKt;
import org.tendiwa.collections.ListsKt;
import org.tendiwa.geometry.bends.Bend;
import org.tendiwa.geometry.bends.BendPropertiesKt;
import org.tendiwa.geometry.points.Point;
import org.tendiwa.geometry.segments.Segment;

/**
 * From a list of vertices forming a polygon, creates {@link OriginalEdgeStart}s
 * and connects those {@link OriginalEdgeStart}s with each other.
 */
final class InitialListOfActiveVertices {
    final List<OriginalEdgeStart> nodes = new ArrayList<>();
    final List<Segment> edges;
    private final int size;

    /**
     * @param vertices List of points going counter-clockwise.
     */
    InitialListOfActiveVertices(
        List<Point> vertices,
        boolean trustCounterClockwise
    ) {
        if (!trustCounterClockwise && !JTSUtils.isYDownCCW(vertices)) {
            vertices = Lists.reverse(vertices);
        }
        vertices = filterExtraVertices(vertices);
        edges = createEdgesBetweenVertices(vertices);
        assert vertices.size() == edges.size();
        createAndConnectNodes(edges);
        nodes.forEach(Node::computeReflexAndBisector);
        this.size = edges.size();
    }

    private List<Point> filterExtraVertices(List<Point> vertices) {
        List<Point> answer = new ArrayList<>();
        for (Triple<? extends Point, ? extends Point, ? extends Point> triple : IterablesKt.getLoopedTriLinks(vertices)) {
            final Bend bend =
                new Bend(triple.getFirst(), triple.getSecond(), triple.getThird());
            if (!BendPropertiesKt.isStraight(bend)) {
                answer.add(bend.getMiddle());
            }
        }
        assert answer.size() == vertices.size();
        return answer;
    }

    int size() {
        return size;
    }

    private void createAndConnectNodes(List<Segment> edges) {
        int l = edges.size();
        OriginalEdgeStart previous = null;
        for (int i = 0; i < l; i++) {
            OriginalEdgeStart node = new OriginalEdgeStart(edges.get(i));
            if (i > 0) {
                node.setPreviousInLav(previous);
                node.setPreviousInitial(previous);
            }
            previous = node;
            nodes.add(node);
        }
        OriginalEdgeStart first = nodes.get(0);
        OriginalEdgeStart last = nodes.get(l - 1);
        first.setPreviousInLav(last);
        first.setPreviousInitial(last);
        nodes.forEach(OriginalEdgeStart::initFace);
    }

    private List<Segment> createEdgesBetweenVertices(List<Point> vertices) {
        int l = vertices.size();
        List<Segment> edges = new ArrayList<>(l);
        for (int i = 0; i < l; i++) {
            edges.add(
                new Segment(
                    vertices.get(i),
                    ListsKt.goForwardLooped(vertices, i, 1)
                )
            );
        }
        return edges;
    }
}
