package com.example.multi.view;

import com.lzy.imagepicker.bean.ImageItem;

import java.util.List;

public interface IMultiImagesSelectCallback {

    void onSelectedImages(List<ImageItem> imageList);
}
