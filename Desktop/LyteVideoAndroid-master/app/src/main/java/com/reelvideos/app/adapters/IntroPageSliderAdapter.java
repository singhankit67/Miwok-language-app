package com.reelvideos.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.reelvideos.app.R;

import org.w3c.dom.Text;

public class IntroPageSliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public IntroPageSliderAdapter(Context context){
        this.context=context;
    }

    //Array List

    public int[] slider_image ={

            R.drawable.intro1,
            R.drawable.intro2,
            R.drawable.intro3
    };

    public String[] slider_text ={
            "Discover #trending\nVideos",
            "Add music to your\nfavourite videos",
            "Express Yourself with\n creativity"
    };

    @Override
    public int getCount() {
        return slider_image.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==(ConstraintLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater =(LayoutInflater) context.getSystemService ( context.LAYOUT_INFLATER_SERVICE );
        View view = layoutInflater.inflate ( R.layout.item_intropage_slider,container,false );

        ImageView IntroImage = (ImageView) view.findViewById ( R.id.introImage );
        TextView IntroText =(TextView) view.findViewById ( R.id.introText );

        IntroImage.setImageResource ( slider_image[position] );
        IntroText.setText ( slider_text[position] );

        container.addView ( view );

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView ( (ConstraintLayout) object);

    }
}
