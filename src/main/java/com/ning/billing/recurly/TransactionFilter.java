package com.ning.billing.recurly;

import com.ning.billing.recurly.model.Transaction;

public class TransactionFilter
{
    private Transaction.State state;
    private Transaction.Type type;

    public Transaction.State getState()
    {
        return state;
    }

    public void setState(final Transaction.State state)
    {
        this.state = state;
    }

    public Transaction.Type getType()
    {
        return type;
    }

    public void setType(final Transaction.Type type)
    {
        this.type = type;
    }
}
