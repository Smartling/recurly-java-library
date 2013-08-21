package com.ning.billing.recurly;

public class PagingParams
{
    private int itemsPerPage;
    private int pageId;

    public int getItemsPerPage()
    {
        return itemsPerPage;
    }

    public void setItemsPerPage(final int itemsPerPage)
    {
        this.itemsPerPage = itemsPerPage;
    }

    public int getPageId()
    {
        return pageId;
    }

    public void setPageId(final int pageId)
    {
        this.pageId = pageId;
    }
}
