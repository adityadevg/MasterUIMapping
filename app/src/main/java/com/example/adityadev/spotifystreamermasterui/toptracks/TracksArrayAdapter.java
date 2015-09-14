package com.example.adityadev.spotifystreamermasterui.toptracks;

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

public class TracksArrayAdapter extends ArrayAdapter<Tracks> {
    private static final String LOG_TAG = TracksArrayAdapter.class.getSimpleName();
    private final int IMAGE_DIMEN = 200;

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the List is the data we want
     * to populate into the lists
     *
     * @param context        The current context. Used to inflate the layout file.
     * @param tracks A List of artist objects to display in a list
     */
    public TracksArrayAdapter(Activity context, List<Tracks> tracks) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, tracks);
    }

    private class ViewHolder {
        TextView albumName;
        TextView trackName;
        ImageView albumImage;
    }


    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate.
     *                    (search online for "android view recycling" to learn more)
     * @param parent The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_tracks,
                    parent, false);
            viewHolder = new ViewHolder();
            viewHolder.albumName = (TextView) convertView.findViewById(R.id
                    .list_item_tracks_name_textview);
            viewHolder.trackName = (TextView) convertView.findViewById(R.id
                    .list_item_tracks_name_textview);
            viewHolder.albumImage = (ImageView) convertView.findViewById(R.id
                    .list_item_tracks_imageview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Gets the artist object from the ArrayAdapter at the appropriate position
        Tracks tracks = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if((null != tracks.getAlbumImageURL()) && (!tracks.getAlbumImageURL().isEmpty())) {
            Picasso.with(getContext()).load(tracks.albumImageURL).resize(IMAGE_DIMEN,IMAGE_DIMEN)
                    .centerCrop().into(viewHolder.albumImage);
        }
        TextView trackName = (TextView) convertView.findViewById(R.id.list_item_tracks_name_textview);
        trackName.setText(tracks.getAlbumName());
        TextView trackID = (TextView) convertView.findViewById(R.id.list_item_tracks_id_textview);
        trackID.setText(tracks.getTrackName());
        return convertView;

    }
}