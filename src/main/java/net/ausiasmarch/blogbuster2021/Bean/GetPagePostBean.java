package net.ausiasmarch.blogbuster2021.Bean;

import java.util.ArrayList;

public class GetPagePostBean {
    
    private ArrayList<PostBean> content = null;
    private int totalElements = 0;
    private int totalPages = 0;

    public GetPagePostBean() {        
    }

    public ArrayList<PostBean> getContent() {
        return content;
    }

    public void setContent(ArrayList<PostBean> content) {
        this.content = content;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

}
