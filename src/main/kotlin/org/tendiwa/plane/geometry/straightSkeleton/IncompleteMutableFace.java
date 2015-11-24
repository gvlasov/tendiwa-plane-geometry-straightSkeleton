package org.tendiwa.plane.geometry.straightSkeleton;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import org.tendiwa.canvas.algorithms.geometry.DrawingArrowKt;
import org.tendiwa.collections.DoublyLinkedNode;
import org.tendiwa.geometry.points.Point;
import org.tendiwa.geometry.polygons.PolygonConstructorsKt;

final class IncompleteMutableFace implements MutableFace, UnderlyingFace {
	final Chain startHalfface;
	//@Nonnull
	final Chain endHalfface;
	private final OriginalEdgeStart edgeStart;
	private final OriginalEdgeStart edgeEnd;
	Chain lastAddedChain;
	private int numberOfSkeletonNodes;
	private final TreeSet<Node> sortedLinkEnds;

	IncompleteMutableFace(OriginalEdgeStart edgeStart, OriginalEdgeStart edgeEnd) {
		this.edgeStart = edgeStart;
		this.edgeEnd = edgeEnd;
		assert edgeStart.next() == edgeEnd;
		sortedLinkEnds = new FaceNodesSorter(edgeStart.vertex, edgeEnd.vertex);
		startHalfface = new Chain(edgeStart, edgeStart, null);
		endHalfface = new Chain(edgeEnd, edgeEnd, startHalfface);

		startHalfface.setNextChain(endHalfface);
		assert startHalfface.nextChain == endHalfface && endHalfface.previousChain == startHalfface
			&& startHalfface.previousChain == null && endHalfface.nextChain == null;
		lastAddedChain = endHalfface;
		numberOfSkeletonNodes = 2; // Initially there are two skeleton nodes: startHalfface start and endHalfface end.
	}

	@Override
	public Node getNodeFromLeft(LeftSplitNode leftNode) {
		Node higher = sortedLinkEnds.higher(leftNode);
		if (higher == null) {
			assert !endHalfface.lastFaceNode().getPayload().isProcessed();
			higher = endHalfface.lastFaceNode().getPayload();
		}
		assert !higher.isProcessed();
		return higher;
	}

	@Override
	public Node getNodeFromRight(RightSplitNode rightNode) {
		Node lower = sortedLinkEnds.lower(rightNode);
		if (lower == null) {
			assert !startHalfface.lastFaceNode().getPayload().isProcessed();
			lower = startHalfface.lastFaceNode().getPayload();
		}
		assert !lower.isProcessed();
		return lower;
	}

	/**
	 * @param end1
	 * 	Order doesn't matter.
	 * @param end2
	 * 	Order doesn't matter.
	 */
	@Override
	public void addLink(Node end1, Node end2) {
		new FaceConstructionStep(this, end1, end2).run();
	}

	@Override
	public void forgetNodeProjection(Node node) {
		sortedLinkEnds.remove(node);
	}

	@Override
	public void addNewSortedEnd(Node end) {
		sortedLinkEnds.add(end);
		assert !end.vertex.equals(startHalfface.firstFaceNode().getPayload().vertex)
			&& !end.vertex.equals(endHalfface.firstFaceNode().getPayload().vertex);
	}

	@Override
	public void setLastAddedChain(Chain chain) {
		lastAddedChain = chain;
	}

	@Override
	public boolean isHalfface(Chain chain) {
		return chain == startHalfface || chain == endHalfface;
	}

	@Override
	public Chain lastAddedChain() {
		return lastAddedChain;
	}

	@Override
	public void increaseNumberOfSkeletonNodes(int d) {
		assert d > 0;
		numberOfSkeletonNodes += d;
	}

	@Override
	public StraightSkeletonFace toPolygon() {
		List<Point> points = new ArrayList<>(numberOfSkeletonNodes);
		DoublyLinkedNode<Node> seed = startHalfface.firstFaceNode();
		assert !seed.hasBothNeighbors();
		Point previousPayload = null;
		for (Node node : seed) {
			if (node.vertex == previousPayload) {
				// This happens at split event points and at starts of half-faces
				continue;
			}
			if (!(points.size() == 0 || !node.vertex.equals(points.get(points.size() - 1)))) {
				assert false;
			}
			points.add(node.vertex);
			previousPayload = node.vertex;
		}

		assertPolygonCorrectness(points);
		return new StraightSkeletonFace(points);
	}

	private void assertPolygonCorrectness(List<Point> points) {
		assert !points.get(0).equals(points.get(points.size() - 1));
		// TODO: Remove this check
		if (JTSUtils.isYDownCCW(points)) {
            PolygonConstructorsKt.Polygon( points ).getSegments().forEach(
                it->
                    DrawingArrowKt.drawArrow(
                        Debug.canvas,
                        it,
                        Color.white,
                        1.0
                    )
            );
			assert false;
		}
	}

	@Override
	public boolean isClosed() {
		return startHalfface.lastFaceNode() == endHalfface.firstFaceNode()
			|| endHalfface.lastFaceNode() == startHalfface.firstFaceNode();
	}

	@Override
	public Iterator<Node> iterator() {
		assert isClosed();
		return startHalfface.firstFaceNode() == startHalfface.lastFaceNode() ?
			startHalfface.firstFaceNode().iterator()
			: endHalfface.firstFaceNode().iterator();
	}

	@Override
	public Chain startHalfface() {
		return startHalfface;
	}
}
