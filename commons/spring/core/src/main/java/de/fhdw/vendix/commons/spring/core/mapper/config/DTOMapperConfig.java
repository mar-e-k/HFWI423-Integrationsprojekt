package de.fhdw.vendix.commons.spring.core.mapper.config;

import de.fhdw.vendix.commons.spring.core.mapper.bean.*;
import org.mapstruct.*;

@MapperConfig(
        uses = {
                AccountDTOMapper.class,
                AccountRoleDTOMapper.class,
                ArticleDTOMapper.class,
                LockDTOMapper.class,
                ReceiptDTOMapper.class,
                ReceiptLineDTOMapper.class,
                ReceiptVoucherDTOMapper.class,
                RegisterDTOMapper.class,
                StoreDTOMapper.class,
                StoreStockDTOMapper.class
        }
)
public interface DTOMapperConfig extends SpringMapperConfig {}