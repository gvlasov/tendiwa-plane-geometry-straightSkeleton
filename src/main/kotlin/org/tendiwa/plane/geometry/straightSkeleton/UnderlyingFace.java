package org.tendiwa.plane.geometry.straightSkeleton;

interface UnderlyingFace {
    Chain startHalfface();

    boolean isHalfface(Chain chain);

    Chain lastAddedChain();

    void increaseNumberOfSkeletonNodes(int d);

    void addNewSortedEnd(Node oneEnd);

    void setLastAddedChain(Chain chain);

    void forgetNodeProjection(Node node);
}
