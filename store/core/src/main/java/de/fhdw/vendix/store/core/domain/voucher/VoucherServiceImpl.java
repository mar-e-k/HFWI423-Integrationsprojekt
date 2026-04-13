package de.fhdw.vendix.store.core.domain.voucher;

import de.fhdw.vendix.commons.spring.data.crud.AbstractCrudService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
class VoucherServiceImpl extends AbstractCrudService<Voucher, Long> implements VoucherService {

    private static final Logger log = LoggerFactory.getLogger(VoucherServiceImpl.class);

    private final VoucherRepository voucherRepository;

    VoucherServiceImpl(VoucherRepository voucherRepository) {
        super(voucherRepository);
        this.voucherRepository = voucherRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Voucher> findByCode(UUID code) {
        if (code == null) {
            return Optional.empty();
        }
        return voucherRepository.findByCode(code);
    }

    @Override
    @Transactional
    public void redeemCode(UUID code) {
        if (code == null) {
            throw new IllegalArgumentException("Parameter 'code' cannot be null");
        }
        Voucher voucher = findByCode(code).orElseThrow(EntityNotFoundException::new);
        Voucher redeemed = voucher.redeem();
        log.atInfo().log("Redeeming receipt voucher with code: {}", code);
        super.update(redeemed);
        log.atInfo().log("Successfully redeemed voucher with code: {}", code);
    }
}