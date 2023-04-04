package com.example.leaves.service.impl;


import com.example.leaves.exceptions.DuplicateEntityException;
import com.example.leaves.exceptions.EntityNotFoundException;
import com.example.leaves.model.dto.TypeEmployeeDto;
import com.example.leaves.model.entity.TypeEmployee;
import com.example.leaves.model.entity.TypeEmployee_;
import com.example.leaves.repository.TypeEmployeeRepository;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.TypeEmployeeService;
import com.example.leaves.service.filter.TypeEmployeeFilter;
import com.example.leaves.util.ListHelper;
import com.example.leaves.util.OffsetBasedPageRequest;
import com.example.leaves.util.PredicateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TypeEmployeeServiceImpl implements TypeEmployeeService {
    private final TypeEmployeeRepository typeRepository;
    private final UserRepository userRepository;

    @Autowired
    public TypeEmployeeServiceImpl(TypeEmployeeRepository typeRepository, UserRepository userRepository) {
        this.typeRepository = typeRepository;
        this.userRepository = userRepository;
    }

    private static void setTypeChanges(TypeEmployeeDto typeDto, TypeEmployee typeToBeUpdated) {
        typeToBeUpdated.setTypeName(typeDto.getTypeName());
        typeToBeUpdated.setDaysLeave(typeDto.getDaysLeave());
    }


    public List<TypeEmployeeDto> getAll() {
        List<TypeEmployeeDto> list = new ArrayList<>();

        typeRepository.findAllByDeletedIsFalse().forEach(e -> list.add(e.toDto()));

        return list;
    }

    public TypeEmployee getByName(String name) {
        return typeRepository.findByTypeName(name);
    }

    @Override
    public TypeEmployeeDto create(TypeEmployeeDto typeDto) {
        //TODO AUTH
        if (typeRepository.existsByTypeName(typeDto.getTypeName())) {
            throw new DuplicateEntityException("Type", typeDto.getTypeName());
        } else if (typeDto.getTypeName() == null || typeDto.getTypeName().isEmpty() || typeDto.getDaysLeave() == 0) {
            throw new IllegalArgumentException("invalid body for typeDto");
        } else {
            TypeEmployee typeEmployee = new TypeEmployee();
            typeEmployee.toEntity(typeDto);
            return typeRepository.save(typeEmployee).toDto();
        }

    }

    public TypeEmployee getById(long typeId) {
        if (typeRepository.findById(typeId) == null) {
            throw new EntityNotFoundException("Type not found", typeId);
        } else {
            return typeRepository.findById(typeId);
        }
    }

    //TODO Authorization
    public TypeEmployee update(TypeEmployeeDto typeDto, long id) {

        if (typeRepository.findByTypeName(typeDto.getTypeName()) != null
                && !typeRepository.findByTypeName(typeDto.getTypeName()).equals(typeRepository.findById(id))) {
            throw new DuplicateEntityException("Type with name", typeDto.getTypeName(), "exists");
        } else if (typeDto.getTypeName() == null || typeDto.getTypeName().isEmpty() || typeDto.getDaysLeave() == 0) {
            throw new IllegalArgumentException();
        } else {
            TypeEmployee typeToBeUpdated = typeRepository.findById(id);
            setTypeChanges(typeDto, typeToBeUpdated);
            typeRepository.save(typeToBeUpdated);
            return typeToBeUpdated;
        }


    }

    public void delete(long id) {
        //TODO make change type method for all employees with this type so it can be deleted?
        typeRepository.markAsDeleted(id);
    }

    @Override
    public void unMarkAsDelete(long id) {
        typeRepository.unMarkAsRemoved(id);
    }


    @Override
    public List<TypeEmployeeDto> getAllFilter(TypeEmployeeFilter filter) {
        switch (filter.getOperation()) {
            case GREATER_THAN:
                return getTypeEmployeeFilteredGreaterThan(filter).getContent();
            case LESS_THAN:
                return getTypeEmployeeFilteredLessThan(filter).getContent();
            default:
                return getTypeEmployeeFilteredEqual(filter).getContent();
        }
    }

    @Override
    public Page<TypeEmployeeDto> getAllFilterPage(TypeEmployeeFilter filter) {
        switch (filter.getOperation()) {
            case GREATER_THAN:
                return getTypeEmployeeFilteredGreaterThan(filter);
            case LESS_THAN:
                return getTypeEmployeeFilteredLessThan(filter);
            default:
                return getTypeEmployeeFilteredEqual(filter);
        }
    }

    private Page<TypeEmployeeDto> getTypeEmployeeFilteredEqual(TypeEmployeeFilter filter) {
        OffsetBasedPageRequest pageRequest = OffsetBasedPageRequest.getOffsetBasedPageRequest(filter);
        return typeRepository.findAll(getSpecification(filter), pageRequest).map(TypeEmployee::toDto);

    }

    private Page<TypeEmployeeDto> getTypeEmployeeFilteredLessThan(TypeEmployeeFilter filter) {
        OffsetBasedPageRequest pageRequest = OffsetBasedPageRequest.getOffsetBasedPageRequest(filter);
        return typeRepository.findAll(getSpecificationLessThan(filter), pageRequest).map(TypeEmployee::toDto);

    }

    private Page<TypeEmployeeDto> getTypeEmployeeFilteredGreaterThan(TypeEmployeeFilter filter) {
        OffsetBasedPageRequest pageRequest = OffsetBasedPageRequest.getOffsetBasedPageRequest(filter);

        return typeRepository.findAll(getSpecificationGreaterThan(filter), pageRequest).map(TypeEmployee::toDto);
    }

    private Specification<TypeEmployee> getSpecification(TypeEmployeeFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                    .in(TypeEmployee_.id, filter.getId())
                    .in(TypeEmployee_.typeName, filter.getTypeName())
                    .in(TypeEmployee_.daysLeave, filter.getDaysLeave())
                    .in(TypeEmployee_.createdAt, filter.getDateCreated())
                    .in(TypeEmployee_.lastModifiedAt, filter.getLastUpdated())
                    .in(TypeEmployee_.createdBy, filter.getCreatedBy())
                    .equal(TypeEmployee_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };
    }

    private Specification<TypeEmployee> getSpecificationLessThan(TypeEmployeeFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                    .lessThan(TypeEmployee_.id, ListHelper.getGreatestNum(filter.getId()))
                    .in(TypeEmployee_.typeName, filter.getTypeName())
                    .lessThan(TypeEmployee_.daysLeave, ListHelper.getGreatestNum(filter.getDaysLeave()))
                    .lessThan(TypeEmployee_.createdAt, ListHelper.getLatestDateTime(filter.getDateCreated()))
                    .lessThan(TypeEmployee_.lastModifiedAt, ListHelper.getLatestDateTime(filter.getLastUpdated()))
                    .in(TypeEmployee_.createdBy, filter.getCreatedBy())
                    .equal(TypeEmployee_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };
    }

    private Specification<TypeEmployee> getSpecificationGreaterThan(TypeEmployeeFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                    .graterThan(TypeEmployee_.id, ListHelper.getGreatestNum(filter.getId()))
                    .in(TypeEmployee_.typeName, filter.getTypeName())
                    .graterThan(TypeEmployee_.daysLeave, ListHelper.getGreatestNum(filter.getDaysLeave()))
                    .greaterThan(TypeEmployee_.createdAt, ListHelper.getLatestDateTime(filter.getDateCreated()))
                    .graterThan(TypeEmployee_.lastModifiedAt, ListHelper.getLatestDateTime(filter.getLastUpdated()))
                    .in(TypeEmployee_.createdBy, filter.getCreatedBy())
                    .equal(TypeEmployee_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };
    }

}
