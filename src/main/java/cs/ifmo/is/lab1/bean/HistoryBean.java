package cs.ifmo.is.lab1.bean;

import cs.ifmo.is.lab1.model.BookCreatureHistory;
import cs.ifmo.is.lab1.repository.BookCreatureHistoryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

@Named
@RequestScoped
public class HistoryBean {

    @Inject
    private BookCreatureHistoryRepository bookCreatureHistoryRepository;

    private List<BookCreatureHistory> historyList;
    private int page = 1;
    private int pageSize = 10;
    private long totalRecords;

    @PostConstruct
    public void init() {
        loadHistoryList();
    }

    public void loadHistoryList() {
        int first = (page - 1) * pageSize;
        historyList = bookCreatureHistoryRepository.findAllPaginated(first, pageSize);
        totalRecords = bookCreatureHistoryRepository.count();
    }

    public List<BookCreatureHistory> getHistoryList() {
        return historyList;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
        loadHistoryList();
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        loadHistoryList();
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) totalRecords / pageSize);
    }

    public void nextPage() {
        if (page < getTotalPages()) {
            page++;
            loadHistoryList();
        }
    }

    public void previousPage() {
        if (page > 1) {
            page--;
            loadHistoryList();
        }
    }
}
