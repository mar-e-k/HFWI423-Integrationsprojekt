package de.fhdw.vendix.store.core.persistance;

import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import de.fhdw.vendix.store.core.persistance.account.AccountMapper;
import de.fhdw.vendix.store.core.persistance.account_role.AccountRoleMapper;
import de.fhdw.vendix.store.core.persistance.article.ArticleMapper;
import de.fhdw.vendix.store.core.persistance.lock.LockMapper;
import de.fhdw.vendix.store.core.persistance.receipt.ReceiptMapper;
import de.fhdw.vendix.store.core.persistance.receipt_line.ReceiptLineMapper;
import de.fhdw.vendix.store.core.persistance.receipt_voucher.ReceiptVoucherMapper;
import de.fhdw.vendix.store.core.persistance.register.RegisterMapper;
import de.fhdw.vendix.store.core.persistance.store.StoreMapper;
import de.fhdw.vendix.store.core.persistance.store_stock.StoreStockMapper;
import org.mapstruct.MapperConfig;

@MapperConfig(
        uses = {
                AccountMapper.class,
                AccountRoleMapper.class,
                ArticleMapper.class,
                LockMapper.class,
                ReceiptLineMapper.class,
                ReceiptMapper.class,
                ReceiptVoucherMapper.class,
                RegisterMapper.class,
                StoreMapper.class,
                StoreStockMapper.class,
        }
)
public interface EntityMapperConfig extends SpringMapperConfig {}