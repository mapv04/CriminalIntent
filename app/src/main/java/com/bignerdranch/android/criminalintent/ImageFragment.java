package com.bignerdranch.android.criminalintent;

import android.app.AlertDialog;
import android.app.Dialog;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class ImageFragment extends DialogFragment {

    private static final String ARG_IMAGE= "image";

    private ImageView imageZoom;

    public static ImageFragment newInstance(String imageFile){
        Bundle args= new Bundle();
        args.putSerializable(ARG_IMAGE, imageFile);

        ImageFragment fragment = new ImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String imageFile = (String) getArguments().getSerializable(ARG_IMAGE);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.image_fragment,null);
        Bitmap bitmap = PictureUtils.getScaleBitmap(imageFile, getActivity());
        imageZoom = (ImageView) v.findViewById(R.id.image_zoom);
        imageZoom.setImageBitmap(bitmap);
        return new AlertDialog.Builder(getActivity()).setView(v).create();

    }

}
