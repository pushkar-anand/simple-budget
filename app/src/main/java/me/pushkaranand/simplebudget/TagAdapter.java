package me.pushkaranand.simplebudget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class TagAdapter extends RecyclerView.Adapter<TagAdapter.RecycleHolder> {
    private List<Tags> TagList;

    TagAdapter(List<Tags> list)
    {
        this.TagList = list;
    }

    void updateTagAdapter(List<Tags> list) {
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
    public void onBindViewHolder(final RecycleHolder holder, int position) {
        final Tags tag = TagList.get(position);
        holder.tagName.setText(tag.getTagName());
        holder.tagSpend.setText(String.valueOf(tag.getTagSpend()));
        final Integer tag_id = tag.getTagId();
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final DatabaseHelper db = DatabaseHelper.getInstance(view.getContext());
                Log.d("RecyleClick TAG: ", String.valueOf(tag_id));
                LayoutInflater li = LayoutInflater.from(view.getContext());
                View prompt = li.inflate(R.layout.new_tag_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setView(prompt);

                final EditText n = prompt.findViewById(R.id.tagNameEdt);
                n.setText(tag.getTagName());
                final EditText l = prompt.findViewById(R.id.tagLimitEdt);
                l.setText(String.valueOf(tag.getTagLimit()));

                builder.setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (TextUtils.isEmpty(n.getText()) || TextUtils.isEmpty(l.getText())) {
                                    Toast.makeText(view.getContext(), "Input name and limit", Toast.LENGTH_SHORT).show();
                                } else {
                                    db.updateTag(tag_id, n.getText().toString(), tag.getTagSpend(), Double.valueOf(l.getText().toString()));
                                    dialogInterface.dismiss();
                                    Intent x = new Intent(view.getContext(), TagsActivity.class);
                                    view.getContext().startActivity(x);
                                }

                            }
                        })
                        .setNegativeButton("Cancel", new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                }
                        );
                AlertDialog alertDialog = builder.create();

                alertDialog.show();
            }
        });
    }

    class RecycleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tagName, tagSpend;
        ConstraintLayout constraintLayout;

        RecycleHolder(View view) {
            super(view);
            tagName = view.findViewById(R.id.tagNameTxt);
            tagSpend = view.findViewById(R.id.spendTxt);
            constraintLayout = view.findViewById(R.id.tagCL);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            Log.d("Clicked: ", "Clicked at " + String.valueOf(id));
        }
    }

}
