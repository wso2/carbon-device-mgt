-- -----------------------------------------------------
-- Table `DM_DEVICE_TYPE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `DM_DEVICE_TYPE` (
  `ID` INT(11) NOT NULL ,
  `NAME` VARCHAR(300) DEFAULT NULL ,
  PRIMARY KEY (`ID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

-- -----------------------------------------------------
-- Table `DM_GROUP`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `DM_GROUP` (
  `ID`                  VARCHAR(20)  NOT NULL,
  `DESCRIPTION`         TEXT         DEFAULT NULL,
  `NAME`                VARCHAR(100) DEFAULT NULL,
  `DATE_OF_ENROLLMENT`  DATETIME     DEFAULT NULL,
  `DATE_OF_LAST_UPDATE` DATETIME     DEFAULT NULL,
  `OWNER`               VARCHAR(45)  DEFAULT NULL,
  TENANT_ID             INTEGER           DEFAULT 0,
  PRIMARY KEY (`ID`)
)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = latin1;

-- -----------------------------------------------------
-- Table `DM_DEVICE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `DM_DEVICE` (
  `ID` VARCHAR(20) NOT NULL ,
  `DESCRIPTION` TEXT DEFAULT NULL ,
  `NAME` VARCHAR(100) DEFAULT NULL ,
  `DEVICE_TYPE_ID` INT(11) DEFAULT NULL ,
  `DEVICE_IDENTIFICATION` VARCHAR(300) DEFAULT NULL ,
  `TENANT_ID` INTEGER DEFAULT 0,
  `GROUP_ID` INT(11) DEFAULT NULL,
  PRIMARY KEY (`ID`) ,
  INDEX `fk_DM_DEVICE_DM_DEVICE_TYPE2_idx` (`DEVICE_TYPE_ID` ASC) ,
  CONSTRAINT `fk_DM_DEVICE_DM_DEVICE_TYPE2`
    FOREIGN KEY (`DEVICE_TYPE_ID` )
    REFERENCES `DM_DEVICE_TYPE` (`ID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  INDEX `fk_DM_DEVICE_DM_GROUP2_idx` (`GROUP_ID` ASC),
  CONSTRAINT `fk_DM_DEVICE_DM_GROUP2`
  FOREIGN KEY (`GROUP_ID`)
  REFERENCES `DM_GROUP` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;
