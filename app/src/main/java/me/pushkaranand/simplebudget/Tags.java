package me.pushkaranand.simplebudget;


class Tags
{
    private Integer tagId;
    private String tagName, tagColor;
    private Double tagSpend, tagLimit;

    public Integer getTagId()
    {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public Double getTagLimit()
    {
        return tagLimit;
    }

    public void setTagLimit(Double tagLimit) {
        this.tagLimit = tagLimit;
    }

    Tags(Integer tagId, String tagName, String tagColor, Double tagSpend, Double tagLimit)
    {
        this.tagId = tagId;
        this.tagName = tagName;
        this.tagColor = tagColor;
        this.tagSpend = tagSpend;
        this.tagLimit = tagLimit;

    }

    Tags(Integer tagId, String tagName, Double tagSpend, Double tagLimit)
    {
        this.tagId = tagId;
        this.tagName = tagName;
        this.tagSpend = tagSpend;
        this.tagLimit = tagLimit;

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
