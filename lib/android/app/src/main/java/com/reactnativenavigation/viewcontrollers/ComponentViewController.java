package com.reactnativenavigation.viewcontrollers;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.View;
import android.content.Context;

import com.reactnativenavigation.interfaces.ScrollEventListener;
import com.reactnativenavigation.parse.Options;
import com.reactnativenavigation.presentation.ComponentPresenter;
import com.reactnativenavigation.presentation.Presenter;
import com.reactnativenavigation.utils.NavigationBarUtils;
import com.reactnativenavigation.utils.StatusBarUtils;
import com.reactnativenavigation.views.ComponentLayout;
import com.reactnativenavigation.views.ReactComponent;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import static com.reactnativenavigation.utils.ObjectUtils.perform;

public class ComponentViewController extends ChildController<ComponentLayout> {
    private final String componentName;
    private ComponentPresenter presenter;
    private final ReactViewCreator viewCreator;

    ReactComponent getComponent() {
        return view;
    }

    public ComponentViewController(final Activity activity,
                                   final ChildControllersRegistry childRegistry,
                                   final String id,
                                   final String componentName,
                                   final ReactViewCreator viewCreator,
                                   final Options initialOptions,
                                   final Presenter presenter,
                                   final ComponentPresenter componentPresenter) {
        super(activity, childRegistry, id, presenter, initialOptions);
        this.componentName = componentName;
        this.viewCreator = viewCreator;
        this.presenter = componentPresenter;
    }

    @Override
    public void setDefaultOptions(Options defaultOptions) {
        super.setDefaultOptions(defaultOptions);
        presenter.setDefaultOptions(defaultOptions);
    }

    @Override
    public ScrollEventListener getScrollEventListener() {
        return perform(view, null, ComponentLayout::getScrollEventListener);
    }

    @Override
    public void onViewAppeared() {
        super.onViewAppeared();
        if (view != null) view.sendComponentStart();
    }

    @Override
    public void onViewDisappear() {
        if (view != null) view.sendComponentStop();
        super.onViewDisappear();
    }

    @Override
    public void sendOnNavigationButtonPressed(String buttonId) {
        getView().sendOnNavigationButtonPressed(buttonId);
    }

    @Override
    public void applyOptions(Options options) {
        if (isRoot()) applyTopInset();
        super.applyOptions(options);
        getView().applyOptions(options);
        presenter.applyOptions(getView(), resolveCurrentOptions(presenter.defaultOptions));
    }

    @Override
    public boolean isViewShown() {
        return super.isViewShown() && view != null && view.isReady();
    }

    @NonNull
    @Override
    protected ComponentLayout createView() {
        view = (ComponentLayout) viewCreator.create(getActivity(), getId(), componentName);
        return (ComponentLayout) view.asView();
    }

    @Override
    public void mergeOptions(Options options) {
        if (options == Options.EMPTY) return;
        presenter.mergeOptions(getView(), options);
        super.mergeOptions(options);
    }

    @Override
    public void applyTopInset() {
        if (view != null) presenter.applyTopInsets(view, getTopInset());
    }

    @Override
    public int getTopInset() {
        int statusBarInset = resolveCurrentOptions().statusBar.isHiddenOrDrawBehind() ? 0 : StatusBarUtils.getStatusBarHeight(getActivity());
        return statusBarInset + perform(getParentController(), 0, p -> p.getTopInset(this));
    }

    @Override
    public void applyBottomInset() {
        if (view != null) presenter.applyBottomInset(view, getBottomInset());
    }

    @Override
    public int getBottomInset() {
        Context activity = getActivity();
        boolean isPortrait = activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        boolean shouldSetBottomInset = !(resolveCurrentOptions().bottomTabsOptions.isHiddenOrDrawBehind() || resolveCurrentOptions().statusBar.isHiddenOrDrawBehind());
        int navigationHeight = NavigationBarUtils.getNavigationBarHeight(activity);
        int navigationBarInset = !shouldSetBottomInset && isPortrait ? 0 : navigationHeight;
        return navigationBarInset;
    }

    private int getLeftInset(WindowInsetsCompat insets) {
        int navigationBarInset = resolveCurrentOptions().statusBar.isHiddenOrDrawBehind() ? 0 : insets.getSystemWindowInsetLeft();
        return navigationBarInset;
    }

    private int getRightInset(WindowInsetsCompat insets) {
        int navigationBarInset = resolveCurrentOptions().statusBar.isHiddenOrDrawBehind() ? 0 : insets.getSystemWindowInsetRight();
        return navigationBarInset;
    }

    @Override
    protected WindowInsetsCompat applyWindowInsets(ViewController view, WindowInsetsCompat insets) {
        ViewCompat.onApplyWindowInsets(view.getView(), insets.replaceSystemWindowInsets(
                getLeftInset(insets),
                insets.getSystemWindowInsetTop(),
                getRightInset(insets),
                Math.max(insets.getSystemWindowInsetBottom() - getBottomInset(), 0)
        ));
        return insets;
    }

    @Override
    public void destroy() {
        final boolean blurOnUnmount = options != null && options.modal.blurOnUnmount.isTrue();
        if (blurOnUnmount) {
            blurActivityFocus();
        }
        super.destroy();
    }

    private void blurActivityFocus() {
        final Activity activity = getActivity();
        final View focusView = activity != null ? activity.getCurrentFocus() : null;
        if (focusView != null) {
            focusView.clearFocus();
        }
    }
}
