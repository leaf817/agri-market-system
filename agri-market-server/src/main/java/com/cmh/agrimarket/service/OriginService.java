package com.cmh.agrimarket.service;

import com.cmh.agrimarket.common.AuthException;
import com.cmh.agrimarket.common.CurrentUser;
import com.cmh.agrimarket.entity.Origin;
import com.cmh.agrimarket.entity.Role;
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

    public List<Origin> list(String scope, CurrentUser current) {
        List<Origin> all = repo.findAll(Sort.by(Sort.Direction.ASC, "id"));
        if ("mine".equalsIgnoreCase(scope)) {
            if (current == null || current.role() != Role.FARMER) {
                throw new AuthException(403, "仅农户可查看自己的产地");
            }
            return all.stream().filter(o -> current.id().equals(o.getFarmerId())).toList();
        }
        if ("manage".equalsIgnoreCase(scope) && current != null && current.role() == Role.FARMER) {
            return all.stream().filter(o -> current.id().equals(o.getFarmerId())).toList();
        }
        return all;
    }

    @Transactional
    public Origin save(Origin origin, CurrentUser current) {
        if (current == null) {
            throw new AuthException(401, "未登录或登录已过期");
        }
        if (current.role() == Role.CONSUMER) {
            throw new AuthException(403, "消费者无产地管理权限");
        }
        if (origin.getId() != null) {
            Origin old = get(origin.getId());
            checkOwner(old, current);
            origin.setFarmerId(old.getFarmerId());
        } else if (current.role() == Role.FARMER) {
            origin.setFarmerId(current.id());
        }
        return repo.save(origin);
    }

    @Transactional
    public void delete(Long id, CurrentUser current) {
        Origin origin = get(id);
        checkOwner(origin, current);
        repo.deleteById(id);
    }

    private Origin get(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("产地不存在: " + id));
    }

    private void checkOwner(Origin origin, CurrentUser current) {
        if (current == null) {
            throw new AuthException(401, "未登录或登录已过期");
        }
        if (current.role() == Role.ADMIN) {
            return;
        }
        if (current.role() == Role.FARMER && current.id().equals(origin.getFarmerId())) {
            return;
        }
        throw new AuthException(403, "只能管理自己的产地");
    }
}
