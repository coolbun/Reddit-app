package com.example.rishabhja.reddit;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.BindingHolder> {

    private List<RedditCardPost> mPost;

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cards_layout, parent, false);
        BindingHolder holder = new BindingHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        Log.d(ListAdapter.class.getName(), "post size: " + mPost.size() + ", position: " + position);
        RedditCardPost post = mPost.get(position);
        holder.getBinding().setVariable(com.example.rishabhja.reddit.BR.cardPost, post);
        holder.getBinding().executePendingBindings();
    }


    public ListAdapter(List<RedditCardPost> posts) {
        mPost = posts;
    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    protected static class BindingHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding binding;

        public BindingHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }
}
