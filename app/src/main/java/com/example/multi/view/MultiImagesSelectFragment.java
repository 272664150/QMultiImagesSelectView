package com.example.multi.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multi.R;
import com.example.multi.dialog.SelectDialog;
import com.example.multi.util.ResUtil;
import com.example.multi.widget.GridSpacingItemDecoration;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.adapter.ImagePickerAdapter;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.ui.ImagePreviewActivity;
import com.lzy.imagepicker.ui.ImagePreviewDelActivity;

import java.util.ArrayList;
import java.util.List;

public class MultiImagesSelectFragment extends Fragment {

    private static final int REQUEST_CODE_SELECT = 0x15;
    private static final int WATCH_IMG_REQUEST_CODE = 0x16;

    private SelectDialog mSelectDialog;
    private RecyclerView mImagesRv;
    private ImagePickerAdapter mAdapter;

    private boolean isDisplayMode;
    private int mSpanCount;
    private int mMaxSelectCount;
    private ArrayList<ImageItem> mImageBeanList;
    private IMultiImagesSelectCallback mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_multi_images_select, container, false);
        initView(contentView);
        return contentView;
    }

    private void initView(View contentView) {
        mImagesRv = contentView.findViewById(R.id.rv_images);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindData();
    }

    private void bindData() {
        mImagesRv.post(() -> {
            // 根据UI稿的比例进行换算，先保证间距和xml的换算基准一致，图片宽高除法运算可丢失部分精度
            int margin = mImagesRv.getWidth() * 15 / 345;
            int imgRealWidth = (mImagesRv.getWidth() - margin * (mSpanCount - 1)) / mSpanCount;

            if (mImageBeanList == null) {
                mImageBeanList = new ArrayList<>();
            }
            if (!isDisplayMode) {
                ImageItem addItem = new ImageItem();
                addItem.path = ResUtil.drawableToUri(getActivity(), R.drawable.icons_add_pictures_default);
                mImageBeanList.add(addItem);
            }
            if (mListener != null) {
                mListener.onSelectedImages((isDisplayMode || hasFull()) ? mImageBeanList : mImageBeanList.subList(0, mImageBeanList.size() - 1));
            }

            mAdapter = new ImagePickerAdapter(getActivity(), mImageBeanList, imgRealWidth);
            mAdapter.setOnItemClickListener((adapter, view, position) -> {
                if (isAddPicture(position)) {
                    createPictureDialog();
                } else {
                    watchBigPic(position);
                }
            });
            mAdapter.setCornerRadius(8);

            mImagesRv.setHasFixedSize(true);
            mImagesRv.setLayoutManager(new GridLayoutManager(getActivity(), mSpanCount));
            mImagesRv.addItemDecoration(new GridSpacingItemDecoration(mSpanCount, margin, false));
            mImagesRv.setAdapter(mAdapter);
        });
    }

    private boolean isAddPicture(int position) {
        return (isDisplayMode || hasFull()) ? false : position == mImageBeanList.size() - 1;
    }

    private int getSelectLimit() {
        return mMaxSelectCount - getRealCount();
    }

    private int getRealCount() {
        return (isDisplayMode || hasFull()) ? mImageBeanList.size() : mImageBeanList.size() - 1;
    }

    private boolean hasFull() {
        return (mImageBeanList.size() == mMaxSelectCount && !mImageBeanList.get(mImageBeanList.size() - 1).path.equals(ResUtil.drawableToUri(getActivity(), R.drawable.icons_add_pictures_default)) && !mImageBeanList.get(mImageBeanList.size() - 1).path.equals(ResUtil.drawableToUri(getActivity(), R.drawable.icons_add_pictures_alarm)))
                || (mMaxSelectCount == 1 && !mImageBeanList.isEmpty() && !mImageBeanList.get(0).path.equals(ResUtil.drawableToUri(getActivity(), R.drawable.icons_add_pictures_default)) && !mImageBeanList.get(0).path.equals(ResUtil.drawableToUri(getActivity(), R.drawable.icons_add_pictures_alarm)));
    }

    private void createPictureDialog() {
        if (getSelectLimit() <= 0) {
            return;
        }

        if (mSelectDialog == null) {
            List<String> names = new ArrayList<>();
            names.add(getResources().getString(R.string.str_take_picture));
            names.add(getResources().getString(R.string.str_obtain_from_album));

            SelectDialog.SelectDialogListener listener = (parent, view, position, id) -> {
                if (0 == position) { // 直接调起相机
                    //打开选择,本次允许选择的数量
                    ImagePicker.getInstance().setSelectLimit(getSelectLimit());
                    Intent intent = new Intent(getActivity(), ImageGridActivity.class);
                    intent.putExtra(ImageGridActivity.EXTRAS_TAKE_PICKERS, true); // 是否是直接打开相机
                    startActivityForResult(intent, REQUEST_CODE_SELECT);
                } else if (1 == position) {
                    //打开选择,本次允许选择的数量  不用考虑 相减 小于 0 的情况，不会执行
                    ImagePicker.getInstance().setSelectLimit(getSelectLimit());
                    Intent intent1 = new Intent(getActivity(), ImageGridActivity.class);
                    /* 如果需要进入选择的时候显示已经选中的图片，
                     * 详情请查看ImagePickerActivity
                     * */
                    startActivityForResult(intent1, REQUEST_CODE_SELECT);
                }
            };
            mSelectDialog = new SelectDialog(getActivity(), R.style.transparentFrameWindowStyle, listener, names);
            mSelectDialog.setPureColor(ContextCompat.getColor(getActivity(), R.color.black));
        }

        if (!isDetached()) {
            mSelectDialog.show();
        }
    }

    private void watchBigPic(int pos) {
        if (isDisplayMode) {
            Intent intent = new Intent(getActivity(), ImagePreviewActivity.class);
            ArrayList<String> pics = new ArrayList<>();
            pics.add(mImageBeanList.get(pos).path);
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, pos);
            intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, pics);
            intent.putExtra(ImagePreviewActivity.IS_ORIGIN, true);
            intent.putExtra(ImagePicker.EXTRA_FROM_ITEMS, true);
            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);
        } else {
            Intent intentPreview = new Intent(getActivity(), ImagePreviewDelActivity.class);
            intentPreview.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, (isDisplayMode || hasFull()) ? mImageBeanList : new ArrayList<>(mImageBeanList.subList(0, mImageBeanList.size() - 1)));
            intentPreview.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, pos);
            intentPreview.putExtra(ImagePicker.EXTRA_FROM_ITEMS, true);
            startActivityForResult(intentPreview, WATCH_IMG_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT && resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            //添加图片返回
            if (data != null) {
                addImages((ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS));
            }
        } else if (requestCode == WATCH_IMG_REQUEST_CODE && resultCode == ImagePicker.RESULT_CODE_BACK) {
            //预览图片返回
            if (data != null) {
                mImageBeanList.clear();
                addImages((ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_ITEMS));
            }
        }
    }

    public void showNormalStatus() {
        ImageItem addItem = mImageBeanList.get(mAdapter.getItemCount() - 1);
        addItem.path = ResUtil.drawableToUri(getActivity(), R.drawable.icons_add_pictures_default);
        mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
    }

    public void showErrorStatus() {
        ImageItem addItem = mImageBeanList.get(mAdapter.getItemCount() - 1);
        addItem.path = ResUtil.drawableToUri(getActivity(), R.drawable.icons_add_pictures_alarm);
        mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
    }

    public void setDisplayMode(boolean displayMode) {
        isDisplayMode = displayMode;
    }

    public void setSpanCount(int spanCount) {
        mSpanCount = spanCount;
    }

    public void setMaxSelectCount(int maxSelectCount) {
        mMaxSelectCount = maxSelectCount;
    }

    public void setDefaultData(ArrayList<ImageItem> imageBeanList) {
        if (mImageBeanList != null) {
            mImageBeanList.clear();
        }
        addImages(imageBeanList);
    }

    private void addImages(ArrayList<ImageItem> imageBeanList) {
        if (mImageBeanList == null) {
            mImageBeanList = new ArrayList<>();
        }
        if (!mImageBeanList.isEmpty() &&
                (mImageBeanList.get(mImageBeanList.size() - 1).path.equals(ResUtil.drawableToUri(getActivity(), R.drawable.icons_add_pictures_default))
                        || (mImageBeanList.get(mImageBeanList.size() - 1).path.equals(ResUtil.drawableToUri(getActivity(), R.drawable.icons_add_pictures_alarm))))) {
            mImageBeanList.remove(mImageBeanList.size() - 1);
        }

        if (imageBeanList != null && !imageBeanList.isEmpty()) {
            mImageBeanList.addAll(imageBeanList);
        }
        if (!isDisplayMode && !hasFull()) {
            ImageItem addItem = new ImageItem();
            addItem.path = ResUtil.drawableToUri(getActivity(), R.drawable.icons_add_pictures_default);
            mImageBeanList.add(addItem);
        }
        mListener.onSelectedImages((isDisplayMode || hasFull()) ? mImageBeanList : mImageBeanList.subList(0, mImageBeanList.size() - 1));
        mAdapter.notifyDataSetChanged();
    }

    public void setImageChangeListener(IMultiImagesSelectCallback listener) {
        mListener = listener;
    }
}
