package com.cmh.agrimarket.service;

import com.cmh.agrimarket.entity.Origin;
import com.cmh.agrimarket.repository.OriginRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OriginService {
    private final OriginRepository repo;

    public List<Origin> list() {
        return repo.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Transactional
    public Origin save(Origin origin) {
        return repo.save(origin);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("产地不存在: " + id);
        }
        repo.deleteById(id);
    }
}
