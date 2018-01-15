package me.pushkaranand.simplebudget;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class TagAdapter extends RecyclerView.Adapter<TagAdapter.RecycleHolder> {
    private List<Tags> TagList;

    TagAdapter(List<Tags> list)
    {
        this.TagList = list;
    }

    public void updateTagAdapter(List<Tags> list) {
        this.TagList = list;
    }

    @Override
    public int getItemCount() {
        return TagList.size();
    }

    @Override
    public RecycleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyleview_tag, parent, false);

        return new RecycleHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecycleHolder holder, int position) {
        Tags tag = TagList.get(position);
        holder.tagName.setText(tag.getTagName());
        holder.tagSpend.setText(String.valueOf(tag.getTagSpend()));
    }

    class RecycleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tagName, tagSpend;

        RecycleHolder(View view) {
            super(view);
            tagName = (TextView) view.findViewById(R.id.tagNameTxt);
            tagSpend = (TextView) view.findViewById(R.id.spendTxt);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            Log.d("Clicked: ", "Clicked at " + String.valueOf(id));
        }
    }

}
