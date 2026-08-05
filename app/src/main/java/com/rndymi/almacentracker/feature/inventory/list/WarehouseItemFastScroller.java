package com.rndymi.almacentracker.feature.inventory.list;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rndymi.almacentracker.R;

final class WarehouseItemFastScroller {

    private static final long HIDE_DELAY_MILLIS = 350L;
    private static final long HIDE_DURATION_MILLIS = 120L;
    private static final int DIRECTION_UP = -1;
    private static final int DIRECTION_DOWN = 1;

    private final RecyclerView recyclerView;
    private final LinearLayoutManager layoutManager;
    private final FloatingActionButton handle;
    private final int topInset;
    private final int touchSlop;

    private int lastDirection = DIRECTION_DOWN;
    private float initialTouchY;
    private float previousTouchY;
    private boolean trackingTouch;
    private boolean movedDuringTouch;
    private boolean neutralIconVisible;

    private final Runnable hideRunnable = this::hide;

    private final RecyclerView.OnScrollListener scrollListener =
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(
                        RecyclerView recyclerView,
                        int dx,
                        int dy
                ) {
                    if (trackingTouch || dy == 0) {
                        return;
                    }

                    lastDirection =
                            dy < 0
                                    ? DIRECTION_UP
                                    : DIRECTION_DOWN;

                    updateDirectionIcon();
                    syncHandlePosition();
                    showTemporarily();
                }
            };

    private final RecyclerView.AdapterDataObserver dataObserver =
            new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    handle.post(
                            () -> {
                                syncHandlePosition();

                                if (!canFastScroll()) {
                                    hideImmediately();
                                }
                            }
                    );
                }

                @Override
                public void onItemRangeInserted(
                        int positionStart,
                        int itemCount
                ) {
                    onChanged();
                }

                @Override
                public void onItemRangeRemoved(
                        int positionStart,
                        int itemCount
                ) {
                    onChanged();
                }

                @Override
                public void onItemRangeChanged(
                        int positionStart,
                        int itemCount
                ) {
                    onChanged();
                }

                @Override
                public void onItemRangeMoved(
                        int fromPosition,
                        int toPosition,
                        int itemCount
                ) {
                    onChanged();
                }
            };

    WarehouseItemFastScroller(
            RecyclerView recyclerView,
            LinearLayoutManager layoutManager,
            FloatingActionButton handle
    ) {
        this.recyclerView = recyclerView;
        this.layoutManager = layoutManager;
        this.handle = handle;

        topInset = Math.round(
                8f * recyclerView
                        .getResources()
                        .getDisplayMetrics()
                        .density
        );
        touchSlop = ViewConfiguration
                .get(recyclerView.getContext())
                .getScaledTouchSlop();

        recyclerView.addOnScrollListener(
                scrollListener
        );

        RecyclerView.Adapter<?> adapter =
                recyclerView.getAdapter();

        if (adapter != null) {
            adapter.registerAdapterDataObserver(
                    dataObserver
            );
        }

        handle.setOnClickListener(
                ignored -> jumpToDirectionalEnd()
        );
        handle.setOnTouchListener(this::onHandleTouch);
        updateDirectionIcon();
        hideImmediately();
    }

    void hideNow() {
        trackingTouch = false;
        hideImmediately();
    }

    void detach() {
        handle.removeCallbacks(hideRunnable);
        handle.animate().cancel();
        recyclerView.removeOnScrollListener(
                scrollListener
        );

        RecyclerView.Adapter<?> adapter =
                recyclerView.getAdapter();

        if (adapter != null) {
            adapter.unregisterAdapterDataObserver(
                    dataObserver
            );
        }

        handle.setOnTouchListener(null);
        handle.setOnClickListener(null);
    }

    private boolean onHandleTouch(
            View view,
            MotionEvent event
    ) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!canFastScroll()) {
                    return false;
                }

                trackingTouch = true;
                movedDuringTouch = false;
                initialTouchY = event.getRawY();
                previousTouchY = initialTouchY;
                handle.removeCallbacks(hideRunnable);
                view.getParent()
                        .requestDisallowInterceptTouchEvent(
                                true
                        );
                return true;

            case MotionEvent.ACTION_MOVE:
                if (!trackingTouch) {
                    return false;
                }

                float currentTouchY = event.getRawY();

                if (currentTouchY < previousTouchY) {
                    lastDirection = DIRECTION_UP;
                } else if (currentTouchY > previousTouchY) {
                    lastDirection = DIRECTION_DOWN;
                }

                previousTouchY = currentTouchY;

                if (Math.abs(
                        currentTouchY - initialTouchY
                ) > touchSlop) {
                    movedDuringTouch = true;
                }

                if (movedDuringTouch) {
                    showNeutralIcon();
                    scrollToRawY(currentTouchY);
                }

                return true;

            case MotionEvent.ACTION_UP:
                if (!trackingTouch) {
                    return false;
                }

                trackingTouch = false;
                view.getParent()
                        .requestDisallowInterceptTouchEvent(
                                false
                        );

                if (!movedDuringTouch) {
                    view.performClick();
                } else {
                    updateDirectionIcon();
                }

                scheduleHide();
                return true;

            case MotionEvent.ACTION_CANCEL:
                trackingTouch = false;
                updateDirectionIcon();
                view.getParent()
                        .requestDisallowInterceptTouchEvent(
                                false
                        );
                scheduleHide();
                return true;

            default:
                return false;
        }
    }

    private void scrollToRawY(float rawY) {
        float fraction = fractionForRawY(rawY);
        int itemCount = itemCount();
        int visibleItemCount = visibleItemCount();
        int targetPosition =
                FastScrollPositionCalculator
                        .positionForFraction(
                                fraction,
                                itemCount,
                                visibleItemCount
                        );

        moveHandleToFraction(fraction);
        layoutManager.scrollToPositionWithOffset(
                targetPosition,
                0
        );
    }

    private float fractionForRawY(float rawY) {
        int[] recyclerLocation = new int[2];
        recyclerView.getLocationOnScreen(
                recyclerLocation
        );

        float halfHandle = handle.getHeight() / 2f;
        float minimumCenter =
                recyclerLocation[1]
                        + topInset
                        + halfHandle;
        float maximumCenter =
                recyclerLocation[1]
                        + recyclerView.getHeight()
                        - recyclerView.getPaddingBottom()
                        - halfHandle;

        if (maximumCenter <= minimumCenter) {
            return 0f;
        }

        return FastScrollPositionCalculator.clamp(
                (rawY - minimumCenter)
                        / (maximumCenter - minimumCenter)
        );
    }

    private void syncHandlePosition() {
        if (!canFastScroll()) {
            return;
        }

        int firstVisiblePosition =
                layoutManager
                        .findFirstVisibleItemPosition();
        int lastVisiblePosition =
                layoutManager
                        .findLastVisibleItemPosition();
        float fraction =
                FastScrollPositionCalculator
                        .fractionForViewport(
                                firstVisiblePosition,
                                lastVisiblePosition,
                                itemCount()
                        );

        moveHandleToFraction(fraction);
    }

    private void moveHandleToFraction(float fraction) {
        float availableDistance =
                Math.max(
                        0,
                        recyclerView.getHeight()
                                - recyclerView.getPaddingBottom()
                                - topInset
                                - handle.getHeight()
                );

        handle.setTranslationY(
                topInset
                        + FastScrollPositionCalculator
                        .clamp(fraction)
                        * availableDistance
        );
    }

    private void jumpToDirectionalEnd() {
        if (!canFastScroll()) {
            return;
        }

        int targetPosition =
                lastDirection == DIRECTION_UP
                        ? 0
                        : FastScrollPositionCalculator
                        .positionForFraction(
                                1f,
                                itemCount(),
                                visibleItemCount()
                        );

        layoutManager.scrollToPositionWithOffset(
                targetPosition,
                0
        );
        handle.post(this::syncHandlePosition);
        showTemporarily();
    }

    private void updateDirectionIcon() {
        neutralIconVisible = false;

        boolean movingUp =
                lastDirection == DIRECTION_UP;

        handle.setImageResource(
                movingUp
                        ? R.drawable.ic_arrow_upward
                        : R.drawable.ic_arrow_downward
        );
        handle.setContentDescription(
                handle.getContext().getString(
                        movingUp
                                ? R.string.fast_scroll_to_top
                                : R.string.fast_scroll_to_bottom
                )
        );
    }

    private void showNeutralIcon() {
        if (neutralIconVisible) {
            return;
        }

        neutralIconVisible = true;
        handle.setImageResource(
                R.drawable.ic_drag_handle
        );
        handle.setContentDescription(
                handle.getContext().getString(
                        R.string.fast_scroll_dragging
                )
        );
    }

    private void showTemporarily() {
        if (!canFastScroll()) {
            hideImmediately();
            return;
        }

        handle.removeCallbacks(hideRunnable);
        handle.animate().cancel();
        handle.setAlpha(1f);
        handle.setVisibility(View.VISIBLE);

        scheduleHide();
    }

    private void scheduleHide() {
        handle.removeCallbacks(hideRunnable);

        if (!trackingTouch) {
            handle.postDelayed(
                    hideRunnable,
                    HIDE_DELAY_MILLIS
            );
        }
    }

    private void hide() {
        if (trackingTouch
                || handle.getVisibility() != View.VISIBLE) {
            return;
        }

        handle.animate().cancel();
        handle.animate()
                .alpha(0f)
                .setDuration(HIDE_DURATION_MILLIS)
                .withEndAction(
                        () -> {
                            if (!trackingTouch
                                    && handle.getAlpha() == 0f) {
                                handle.setVisibility(View.GONE);
                            }
                        }
                )
                .start();
    }

    private void hideImmediately() {
        handle.removeCallbacks(hideRunnable);
        handle.animate().cancel();
        handle.setAlpha(0f);
        handle.setVisibility(View.GONE);
    }

    private boolean canFastScroll() {
        return itemCount() > visibleItemCount()
                && (recyclerView.canScrollVertically(-1)
                || recyclerView.canScrollVertically(1));
    }

    private int itemCount() {
        RecyclerView.Adapter<?> adapter =
                recyclerView.getAdapter();

        return adapter == null
                ? 0
                : adapter.getItemCount();
    }

    private int visibleItemCount() {
        int first =
                layoutManager
                        .findFirstVisibleItemPosition();
        int last =
                layoutManager
                        .findLastVisibleItemPosition();

        return first == RecyclerView.NO_POSITION
                || last == RecyclerView.NO_POSITION
                ? 1
                : Math.max(1, last - first + 1);
    }
}
