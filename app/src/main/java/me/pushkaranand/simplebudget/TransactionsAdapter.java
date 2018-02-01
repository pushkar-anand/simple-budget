package me.pushkaranand.simplebudget;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
        final Transactions txn = TransactionList.get(position);
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
                Log.d("RecyleClick: ", String.valueOf(txn_id));
                Intent intent = new Intent(context, ViewTransaction.class);
                intent.putExtra("TXN_ID", txn_id);
                context.startActivity(intent);
            }
        });

        holder.constraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d("RecyleLongClick: ", String.valueOf(txn_id));
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Options");

                builder.setCancelable(false)
                        .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(context, ViewTransaction.class);
                                intent.putExtra("TXN_ID", txn_id);
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                AlertDialog.Builder b = new AlertDialog.Builder(context);
                                b.setTitle("Delete");
                                b.setMessage("Are you sure you want to delete this transaction???");

                                b.setCancelable(false)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                                ProgressDialog progressDialog = new ProgressDialog(context);
                                                progressDialog.setMessage("Deleting");
                                                progressDialog.show();

                                                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
                                                if (databaseHelper.deleteTransaction(txn_id) > 0) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();

                                                    Intent intent = new Intent(context, MainActivity.class);
                                                    intent.putExtra("TXN_ID", txn_id);
                                                    context.startActivity(intent);
                                                } else {
                                                    Toast.makeText(context, "Some error occurred.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        });

                                AlertDialog alert = b.create();
                                alert.show();

                                dialogInterface.dismiss();
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();


                return false;
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return TransactionList.size();
    }

    void updateData(List<Transactions> TransactionList)
    {
        this.TransactionList = TransactionList;
    }

    @SuppressWarnings("unused")
    class RecycleHolder extends RecyclerView.ViewHolder {
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
    }
}
