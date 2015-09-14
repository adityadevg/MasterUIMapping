package com.example.adityadev.spotifystreamermasterui.artistsmodel;

/**
 * Created by adityadev.
 */

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.adityadev.spotifystreamermasterui.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ArtistArrayAdapter extends ArrayAdapter<Artist> {
    private static final String LOG_TAG = ArtistArrayAdapter.class.getSimpleName();
    private final int IMAGE_DIMEN = 200;

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the List is the data we want
     * to populate into the lists
     *
     * @param context The current context. Used to inflate the layout file.
     * @param artists A List of artist objects to display in a list
     */
    public ArtistArrayAdapter(Activity context, List<Artist> artists) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, artists);
    }

    private class ViewHolder {
        TextView artistName;
        ImageView artistImage;
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate.
     *                    (search online for "android view recycling" to learn more)
     * @param parent      The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artists,
                    parent, false);
            viewHolder = new ViewHolder();
            viewHolder.artistName = (TextView) convertView.findViewById(R.id.list_item_artists_textview);
            viewHolder.artistImage = (ImageView) convertView.findViewById(R.id.list_item_artists_imageview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        // Gets the artist object from the ArrayAdapter at the appropriate position
        Artist artist = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.


        if (!artist.imageURL.isEmpty()) {
            Picasso.with(getContext()).load(artist.imageURL).resize(200, 200).centerCrop().into
                    (viewHolder
                            .artistImage);
        }
        TextView artistName = (TextView) convertView.findViewById(R.id.list_item_artists_textview);
        viewHolder.artistName.setText(artist.artistName);
        return convertView;
    }
}