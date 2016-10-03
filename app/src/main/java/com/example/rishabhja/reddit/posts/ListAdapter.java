package com.example.rishabhja.reddit.posts;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.example.rishabhja.reddit.R;
import com.example.rishabhja.reddit.databinding.CardsLayoutBinding;
import com.example.rishabhja.reddit.viewmodels.PostViewModel;

import java.util.List;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.BindingHolder> {

    private List<PostViewModel> mPost;
    private NotificationsInterface notify;

    public interface NotificationsInterface {
        public void notifyDeletion(PostViewModel post);

        public boolean notifyVote(PostViewModel post, int dir);
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cards_layout, parent, false);
        BindingHolder holder = new BindingHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, final int position) {
        final PostViewModel post = mPost.get(position);
        final CardsLayoutBinding binding = (CardsLayoutBinding) holder.getBinding();
        binding.setCardPost(post);

        //sets onClick for popUpMenu
        binding.popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.hidepost:
                                //notify activity to handle deletion of post
                                notify.notifyDeletion(mPost.get(position));
                                boolean deleted = mPost.remove(mPost.get(position));
                                if (!deleted)
                                    Log.e("Error", "Post not deleted");
                                notifyDataSetChanged();
                                return true;
                            case R.id.sharepost:
                                Intent sharePost = new Intent(Intent.ACTION_SEND);
                                sharePost.putExtra(Intent.EXTRA_TEXT, post.title);
                                sharePost.setType("text/plain");
                                view.getContext().startActivity(sharePost);
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        //sets OnClick for upvote
        binding.upvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPost.get(position).alreadyDownvoted) {

                } else if (mPost.get(position).alreadyUpvoted) {
                    boolean upvote = notify.notifyVote(mPost.get(position), 0);
                    if (upvote) {
                        mPost.get(position).upvotes--;
                        mPost.get(position).alreadyUpvoted = false;
                        binding.downvote.setBackgroundColor
                                (view.getContext().getColor(R.color.default_vote));
                    }
                    view.setBackgroundColor(view.getContext().getColor(R.color.default_vote));
                } else {
                    boolean upvote = notify.notifyVote(mPost.get(position), 1);
                    if (upvote) {
                        mPost.get(position).upvotes++;
                        mPost.get(position).alreadyUpvoted = true;
                        view.setBackgroundColor(view.getContext().getColor(R.color.voted));
                        binding.downvote.setBackgroundColor
                                (view.getContext().getColor(R.color.disabled));
                    }
                }
                notifyDataSetChanged();
            }
        });


        //sets OnClick for downvoted
        binding.downvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPost.get(position).alreadyUpvoted) {
                } else if (mPost.get(position).alreadyDownvoted) {
                    boolean upvote = notify.notifyVote(mPost.get(position), 0);
                    if (upvote) {
                        mPost.get(position).upvotes++;
                        mPost.get(position).alreadyDownvoted = false;
                    }
                    view.setBackgroundColor(view.getContext().getColor(R.color.default_vote));
                    binding.upvote.setBackgroundColor
                            (view.getContext().getColor(R.color.default_vote));
                } else {
                    boolean upvote = notify.notifyVote(mPost.get(position), -1);
                    if (upvote) {
                        mPost.get(position).upvotes--;
                        mPost.get(position).alreadyDownvoted = true;
                        view.setBackgroundColor(view.getContext().getColor(R.color.voted));
                        binding.upvote.setBackgroundColor
                                (view.getContext().getColor(R.color.disabled));
                    }
                }
                notifyDataSetChanged();
            }
        });
        binding.executePendingBindings();
    }


    public ListAdapter(List<PostViewModel> posts, NotificationsInterface notificationsInterface) {
        mPost = posts;
        this.notify = notificationsInterface;
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
