// Generated by data binding compiler. Do not edit!
package com.example.schoolapp.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;
import com.example.schoolapp.R;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class ModelBinding extends ViewDataBinding {
  @NonNull
  public final CardView contentCard;

  @NonNull
  public final LinearLayout likeLayout;

  @NonNull
  public final TextView mContentTV;

  @NonNull
  public final ShapedImageView mImageView;

  @NonNull
  public final TextView mTitleTV;

  @NonNull
  public final ImageView mViewsImg;

  @NonNull
  public final TextView mViewsTV;

  @NonNull
  public final ConstraintLayout relativeLayout3;

  protected ModelBinding(Object _bindingComponent, View _root, int _localFieldCount,
      CardView contentCard, LinearLayout likeLayout, TextView mContentTV,
      ShapedImageView mImageView, TextView mTitleTV, ImageView mViewsImg, TextView mViewsTV,
      ConstraintLayout relativeLayout3) {
    super(_bindingComponent, _root, _localFieldCount);
    this.contentCard = contentCard;
    this.likeLayout = likeLayout;
    this.mContentTV = mContentTV;
    this.mImageView = mImageView;
    this.mTitleTV = mTitleTV;
    this.mViewsImg = mViewsImg;
    this.mViewsTV = mViewsTV;
    this.relativeLayout3 = relativeLayout3;
  }

  @NonNull
  public static ModelBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup root,
      boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.model, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ModelBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup root,
      boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ModelBinding>inflateInternal(inflater, R.layout.model, root, attachToRoot, component);
  }

  @NonNull
  public static ModelBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.model, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ModelBinding inflate(@NonNull LayoutInflater inflater, @Nullable Object component) {
    return ViewDataBinding.<ModelBinding>inflateInternal(inflater, R.layout.model, null, false, component);
  }

  public static ModelBinding bind(@NonNull View view) {
    return bind(view, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.bind(view, component)
   */
  @Deprecated
  public static ModelBinding bind(@NonNull View view, @Nullable Object component) {
    return (ModelBinding)bind(component, view, R.layout.model);
  }
}
