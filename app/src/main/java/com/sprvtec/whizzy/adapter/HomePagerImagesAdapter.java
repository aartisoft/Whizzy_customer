package com.sprvtec.whizzy.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.imageloaderstuff.ImageLoader;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.HomeImage;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.PagerAdapter;

/**
 * Created by Haritha on 3/25/2017.
 */

public class HomePagerImagesAdapter extends PagerAdapter {

    private Context context;

    private List<HomeImage> imagesobjects;
    private ImageLoader imageLoader;


    public HomePagerImagesAdapter(Context context, List<HomeImage> imagesobjects) {
        this.context = context;
        this.imagesobjects = imagesobjects;
        imageLoader = new ImageLoader(context);

    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        View viewItem = inflater.inflate(R.layout.custom_images, container, false);
        ImageView imageViewprpe = viewItem.findViewById(R.id.imageViewwee);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            String imageURL = Webservice.HOME_IMAGE_PATH + imagesobjects.get(position).image_url;
            Picasso.with(context).load(imageURL).into(imageViewprpe);
        } else
            imageLoader.DisplayImage(Webservice.HOME_IMAGE_PATH + imagesobjects.get(position).image_url, imageViewprpe, R.drawable.transparent);

        imageViewprpe.setOnClickListener(v -> {

        });

        container.addView(viewItem);

        return viewItem;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return imagesobjects.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        // TODO Auto-generated method stub
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // TODO Auto-generated method stub
        container.removeView((View) object);
    }


}
