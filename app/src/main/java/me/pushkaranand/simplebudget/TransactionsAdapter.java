package me.pushkaranand.simplebudget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.RecycleHolder>
{
    //private static RecyclerViewClickListener itemListener;
    private final Context context;
    private List<Transactions> TransactionList;

    public TransactionsAdapter(Context context, List<Transactions> TransactionList)
    {
        this.context = context;
        this.TransactionList = TransactionList;
    }

    @Override
    public RecycleHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_recycleview, parent, false);

        return new RecycleHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecycleHolder holder, int position)
    {
        Transactions txn = TransactionList.get(position);
        final Integer txn_id = txn.getTxn_id();
        holder.date.setText(txn.getTxn_date());
       // holder.notes.setText(txn.getTxn_notes());
        holder.category.setText(txn.getTxn_category());
        if (txn.getTxn_type().equals("CREDIT"))
        {
            String str = "+"+String.valueOf(txn.getTxn_amount());
            holder.amount.setText(str);
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.credit_bg));
        }
        else if (txn.getTxn_type().equals("DEBIT"))
        {
            String str = "-"+String.valueOf(txn.getTxn_amount());
            holder.amount.setText(str);
            holder.itemView.setBackgroundColor(Color.RED);
        }
        //holder.onClick(holder.itemView);
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.d("REcyleClick: ", String.valueOf(txn_id));
                Intent intent = new Intent(context, ViewTransaction.class);
                intent.putExtra("TXN_ID", txn_id);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return TransactionList.size();
    }

    public void updateData(List<Transactions> TransactionList)
    {
        this.TransactionList = TransactionList;
    }

    @SuppressWarnings("unused")
    class RecycleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView amount;
        final TextView date;
        final TextView category;
        final ConstraintLayout constraintLayout;
        TextView notes;

        RecycleHolder(View view) {
            super(view);
            amount = view.findViewById(R.id.AmountTxt);
            date = view.findViewById(R.id.datetxt);
            //notes = (TextView) view.findViewById(R.id.notesTxt);
            category = view.findViewById(R.id.categoryTxt);
            constraintLayout = view.findViewById(R.id.recycleConstraint);
        }

        @Override
        public void onClick(View view) {
            //itemListener.recyclerViewListClicked(v, this.getPosition());
            int id = view.getId();
            Log.d("Clicked: ", "Clicked at " + String.valueOf(id));
        }
    }
}
