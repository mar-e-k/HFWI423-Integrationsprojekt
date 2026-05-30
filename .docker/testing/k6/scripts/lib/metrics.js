import {Counter} from 'k6/metrics';

export const bonsCreatedMetric = new Counter('vendix_bons_created');
export const bonsFailedMetric = new Counter('vendix_bons_failed');
export const checkoutsMetric = new Counter('vendix_checkouts');
export const bonPrintsMetric = new Counter('vendix_bon_prints');
export const articlesAddedMetric = new Counter('vendix_articles_added');
export const cancelsMetric = new Counter('vendix_cancels');
export const depositsMetric = new Counter('vendix_deposits');
export const discountsMetric = new Counter('vendix_discounts');
export const voucherChecksMetric = new Counter('vendix_voucher_checks');
export const voucherRedeemsMetric = new Counter('vendix_voucher_redeems');
export const gtinScansMetric = new Counter('vendix_gtin_scans');