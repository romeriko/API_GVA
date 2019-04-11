package com.project.gva.model;

import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class PageObject {
    private int page = 0;
    private int size = 10;
    private String field = "date";
    private Sort.Direction order = Sort.Direction.DESC;
}
