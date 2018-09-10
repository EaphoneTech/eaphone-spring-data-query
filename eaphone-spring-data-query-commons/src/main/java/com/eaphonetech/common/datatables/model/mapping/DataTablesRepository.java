package com.eaphonetech.common.datatables.model.mapping;

import java.io.Serializable;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface DataTablesRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {

}
