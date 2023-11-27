DELETE FROM company_trade;

ALTER TABLE company_trade
    DROP CONSTRAINT company_trade_trade_fkey;

DROP TABLE location_trade;
DROP TABLE trade;
