// ═══════════════════════════════════════════════════════════════════════════
// lib/metrics.js – Custom k6-Metriken für Business-Operationen
// ═══════════════════════════════════════════════════════════════════════════
//
// Diese Metriken werden via experimental-prometheus-rw nach Prometheus gepusht.
// Prometheus-Namen bekommen das Prefix "k6_" automatisch.
//
// Verfügbare Metriken nach Prometheus-Import:
//   k6_vendix_bons_created
//   k6_vendix_bons_failed
//   k6_vendix_articles_added
//   k6_vendix_cancels
//   k6_vendix_deposits
//   k6_vendix_discounts

import { Counter } from 'k6/metrics';

export const bonsCreatedMetric    = new Counter('vendix_bons_created');
export const bonsFailedMetric     = new Counter('vendix_bons_failed');
export const articlesAddedMetric  = new Counter('vendix_articles_added');
export const cancelsMetric        = new Counter('vendix_cancels');
export const depositsMetric       = new Counter('vendix_deposits');
export const discountsMetric      = new Counter('vendix_discounts');