package me.pushkaranand.simplebudget;


class Tags
{
    private String tagName, tagColor;
    private Double tagSpend;


    public Tags(String tagName, Double tagSpend)
    {
        this.tagName = tagName;
        this.tagSpend = tagSpend;
    }

    public String getTagName()
    {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Double getTagSpend() {
        return tagSpend;
    }

    public void setTagSpend(Double tagSpend) {
        this.tagSpend = tagSpend;
    }

    public String getTagColor() {
        return tagColor;
    }

    public void setTagColor(String tagColor) {
        this.tagColor = tagColor;
    }
}
