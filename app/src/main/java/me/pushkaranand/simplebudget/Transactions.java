package me.pushkaranand.simplebudget;


@SuppressWarnings("unused")
class Transactions
{
    private Integer txn_id;
    private String txn_date, txn_category, txn_type, txn_notes;
    private Double txn_amount;

    Transactions(Integer txn_id, String txn_date, String txn_category, String txn_type, String txn_notes, Double txn_amount) {
        this.txn_id = txn_id;
        this.txn_date = txn_date;
        this.txn_category = txn_category;
        this.txn_type = txn_type;
        this.txn_notes = txn_notes;
        this.txn_amount = txn_amount;
    }

    public String getTxn_date() {
        return txn_date;
    }

    public void setTxn_date(String txn_date) {
        this.txn_date = txn_date;
    }

    public String getTxn_category() {
        return txn_category;
    }

    public void setTxn_category(String txn_category) {
        this.txn_category = txn_category;
    }

    public String getTxn_type() {
        return txn_type;
    }

    public void setTxn_type(String txn_type) {
        this.txn_type = txn_type;
    }

    public String getTxn_notes() {
        return txn_notes;
    }

    public void setTxn_notes(String txn_notes) {
        this.txn_notes = txn_notes;
    }

    public Integer getTxn_id() {
        return txn_id;
    }

    public void setTxn_id(Integer txn_id) {
        this.txn_id = txn_id;
    }

    public Double getTxn_amount() {
        return txn_amount;
    }

    public void setTxn_amount(Double txn_amount) {
        this.txn_amount = txn_amount;
    }
}
