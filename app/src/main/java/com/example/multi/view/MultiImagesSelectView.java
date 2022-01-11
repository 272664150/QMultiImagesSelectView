package com.example.multi.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.multi.R;
import com.example.multi.manager.ActivityStackManager;
import com.example.multi.util.ScreenUtils;
import com.lzy.imagepicker.bean.ImageItem;

import java.util.ArrayList;
import java.util.List;

public class MultiImagesSelectView extends RelativeLayout implements IComponentView, IMultiImagesSelectCallback {

    private ConstraintLayout mConstraintLayout;
    private ImageView mRequiredIv;
    private TextView mLeftTitleTv;
    private ImageView mLeftIconIv;
    private TextView mRightTitleTv;
    private TextView mSubTitleTv;

    private MultiImagesSelectFragment mImagesFragment;

    private boolean isRequired;
    private String mLeftIconVisibility;
    private String mLeftTitleStr;
    private Drawable mLeftIcon;
    private String mRightTitleVisibility;
    private String mRightTitleStr;
    private String mSubTitleVisibility;
    private String mSubTitleStr;
    private boolean isDisplayMode;
    private int mSpanCount;
    private int mMaxSelectCount;
    private List<ImageItem> mImageBeanList;

    public MultiImagesSelectView(Context context) {
        this(context, null);
    }

    public MultiImagesSelectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiImagesSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        parseXmlAttributes(context, attrs);
    }

    @Override
    public void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_multi_images_select, this);

        mConstraintLayout = findViewById(R.id.cl_multi_images);
        mRequiredIv = findViewById(R.id.iv_required);
        mLeftTitleTv = findViewById(R.id.tv_left_title);
        mLeftIconIv = findViewById(R.id.iv_left_Icon);
        mRightTitleTv = findViewById(R.id.tv_right_title);
        mSubTitleTv = findViewById(R.id.tv_sub_title);

        AppCompatActivity currentActivity = (AppCompatActivity) ActivityStackManager.getInstance().getCurrentActivity();
        if (currentActivity == null) {
            return;
        }

        FragmentManager fragmentManager = currentActivity.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (mImagesFragment == null) {
            mImagesFragment = new MultiImagesSelectFragment();
            mImagesFragment.setImageChangeListener(this);
            int genId = View.generateViewId();
            FrameLayout flContainer = new FrameLayout(context);
            flContainer.setId(genId);
            mConstraintLayout.addView(flContainer);

            ConstraintSet set = new ConstraintSet();
            set.connect(genId, ConstraintSet.TOP, R.id.tv_sub_title, ConstraintSet.BOTTOM, ScreenUtils.dpToPxInt(context, 10f));
            set.connect(genId, ConstraintSet.LEFT, R.id.tv_sub_title, ConstraintSet.LEFT);
            set.constrainWidth(genId, ConstraintSet.MATCH_CONSTRAINT);
            set.constrainHeight(genId, ConstraintSet.WRAP_CONTENT);
            set.applyTo(mConstraintLayout);

            transaction.add(genId, mImagesFragment);
        } else {
            transaction.show(mImagesFragment);
        }
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void parseXmlAttributes(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.MultiImagesSelectView);
        if (typedArray == null) {
            return;
        }

        isRequired = typedArray.getBoolean(R.styleable.MultiImagesSelectView_required, false);
        mLeftTitleStr = typedArray.getString(R.styleable.MultiImagesSelectView_left_title_text);
        mLeftIconVisibility = typedArray.getString(R.styleable.MultiImagesSelectView_left_icon_visibility);
        mLeftIcon = typedArray.getDrawable(R.styleable.MultiImagesSelectView_left_icon);
        mRightTitleVisibility = typedArray.getString(R.styleable.MultiImagesSelectView_right_title_visibility);
        mRightTitleStr = typedArray.getString(R.styleable.MultiImagesSelectView_right_title_text);
        mSubTitleVisibility = typedArray.getString(R.styleable.MultiImagesSelectView_sub_title_visibility);
        mSubTitleStr = typedArray.getString(R.styleable.MultiImagesSelectView_sub_title_text);
        isDisplayMode = typedArray.getBoolean(R.styleable.MultiImagesSelectView_display_mode, false);
        mSpanCount = typedArray.getInteger(R.styleable.MultiImagesSelectView_span_count, 3);
        mMaxSelectCount = typedArray.getInteger(R.styleable.MultiImagesSelectView_max_select_count, 6);
        typedArray.recycle();

        bindAttributes();
    }

    @Override
    public int getDefaultInputType() {
        return 0;
    }

    private void bindAttributes() {
        setRequired(isRequired);
        if (!TextUtils.isEmpty(mLeftTitleStr)) {
            setLeftTitle(mLeftTitleStr);
        }
        if (mLeftIcon != null) {
            setLeftIcon(mLeftIcon);
        }
        if (!TextUtils.isEmpty(mRightTitleStr)) {
            setRightTitle(mRightTitleStr);
        }
        if (!TextUtils.isEmpty(mSubTitleStr)) {
            setSubTitle(mSubTitleStr);
        } else {
            // 副标题无内容默认不显示
            setSubTitleVisibility(GONE);
        }
        if (!TextUtils.isEmpty(mLeftIconVisibility)) {
            if ("gone".equals(mLeftIconVisibility)) {
                setLeftIconVisibility(GONE);
            } else if ("invisible".equals(mLeftIconVisibility)) {
                setLeftIconVisibility(INVISIBLE);
            } else {
                setLeftIconVisibility(VISIBLE);
            }
        }
        if (!TextUtils.isEmpty(mRightTitleVisibility)) {
            if ("gone".equals(mRightTitleVisibility)) {
                setRightTitleVisibility(GONE);
            } else if ("invisible".equals(mRightTitleVisibility)) {
                setRightTitleVisibility(INVISIBLE);
            } else {
                setRightTitleVisibility(VISIBLE);
            }
        }
        if (!TextUtils.isEmpty(mSubTitleVisibility)) {
            if ("gone".equals(mSubTitleVisibility)) {
                setSubTitleVisibility(GONE);
            } else if ("invisible".equals(mSubTitleVisibility)) {
                setSubTitleVisibility(INVISIBLE);
            } else {
                setSubTitleVisibility(VISIBLE);
            }
        }
        setDisplayMode(isDisplayMode);
        setSpanCount(mSpanCount);
        setMaxSelectCount(mMaxSelectCount);
    }

    public void setRequired(boolean required) {
        if (required) {
            mRequiredIv.setVisibility(VISIBLE);
        } else {
            mRequiredIv.setVisibility(GONE);
        }
    }

    public void showNormalStatus() {
        if (isDisplayMode) {
            return;
        }
        mImagesFragment.showNormalStatus();
    }

    public void showErrorStatus() {
        if (isDisplayMode) {
            return;
        }
        mImagesFragment.showErrorStatus();
    }

    public void setLeftIconVisibility(int visibility) {
        mLeftIconIv.setVisibility(visibility);
    }

    public void setLeftTitle(int titleId) {
        mLeftTitleTv.setText(titleId);
    }

    public void setLeftTitle(String title) {
        mLeftTitleTv.setText(title);
    }

    public void setLeftTitle(CharSequence title) {
        mLeftTitleTv.setText(title);
    }

    public CharSequence getLeftTitleText() {
        return mLeftTitleTv.getText();
    }

    public void setLeftIcon(Drawable drawable) {
        mLeftIconIv.setBackground(drawable);
    }

    public void setRightTitleVisibility(int visibility) {
        mRightTitleTv.setVisibility(visibility);
    }

    public void setRightTitle(int titleId) {
        mRightTitleTv.setText(titleId);
        setRightTitleVisibility(VISIBLE);
    }

    public void setRightTitle(String title) {
        mRightTitleTv.setText(title);
        setRightTitleVisibility(VISIBLE);
    }

    public void setRightTitle(CharSequence title) {
        mRightTitleTv.setText(title);
        setRightTitleVisibility(VISIBLE);
    }

    public void setSubTitleVisibility(int visibility) {
        mSubTitleTv.setVisibility(visibility);
    }

    public void setSubTitle(int titleId) {
        mSubTitleTv.setText(titleId);
        setSubTitleVisibility(VISIBLE);
    }

    public void setSubTitle(String title) {
        mSubTitleTv.setText(title);
        setSubTitleVisibility(VISIBLE);
    }

    public void setSubTitle(CharSequence title) {
        mSubTitleTv.setText(title);
        setSubTitleVisibility(VISIBLE);
    }

    public void setDisplayMode(boolean displayMode) {
        mImagesFragment.setDisplayMode(displayMode);
        if (displayMode) {
            setRightTitleVisibility(INVISIBLE);
        }
    }

    public void setSpanCount(int spanCount) {
        mImagesFragment.setSpanCount(spanCount);
    }

    public void setMaxSelectCount(int maxSelectCount) {
        mMaxSelectCount = maxSelectCount;
        // 最多一张图片时，产品要求不显示"x/y张"
        if (mMaxSelectCount <= 1) {
            setRightTitleVisibility(INVISIBLE);
        }
        mImagesFragment.setMaxSelectCount(maxSelectCount);
    }

    public void setDefaultData(ArrayList<ImageItem> imageBeanList) {
        if (imageBeanList == null || imageBeanList.isEmpty()) {
            return;
        }
        postDelayed(() -> mImagesFragment.setDefaultData(imageBeanList), 500);
    }

    @Override
    public void onSelectedImages(List<ImageItem> imageList) {
        mImageBeanList = imageList;
        mRightTitleTv.setText((imageList == null ? 0 : imageList.size()) + "/" + mMaxSelectCount);
    }

    public List<ImageItem> getImageList() {
        return mImageBeanList;
    }
}
