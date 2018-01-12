package me.pushkaranand.simplebudget;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.RecycleHolder>
{
    private List<Transactions> TransactionList;

    class RecycleHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView amount, date, notes, category;

        RecycleHolder(View view)
        {
            super(view);
            amount = (TextView) view.findViewById(R.id.AmountTxt);
            date = (TextView) view.findViewById(R.id.datetxt);
            //notes = (TextView) view.findViewById(R.id.notesTxt);
            category = (TextView) view.findViewById(R.id.categoryTxt);
        }
        @Override
        public void onClick(View view)
        {
            int id = view.getId();
            Log.d("Clicked: ", "Clicked at "+String.valueOf(id));
        }
    }

    public TransactionsAdapter(List<Transactions> TransactionList)
    {
        this.TransactionList = TransactionList;
    }

    @Override
    public RecycleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_recycleview, parent, false);

        return new RecycleHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecycleHolder holder, int position)
    {
        Transactions txn = TransactionList.get(position);
        holder.date.setText(txn.getTxn_date());
       // holder.notes.setText(txn.getTxn_notes());
        holder.category.setText(txn.getTxn_category());
        if (txn.getTxn_type().equals("CREDIT"))
        {
            String str = "+"+String.valueOf(txn.getTxn_amount());
            holder.amount.setText(str);
            holder.itemView.setBackgroundColor(Color.GREEN);
        }
        else if (txn.getTxn_type().equals("DEBIT"))
        {
            String str = "-"+String.valueOf(txn.getTxn_amount());
            holder.amount.setText(str);
            holder.itemView.setBackgroundColor(Color.RED);
        }

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
}
