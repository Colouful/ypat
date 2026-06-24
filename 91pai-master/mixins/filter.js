/*
 * @Author: shawn
 * @LastEditTime: 2019-12-17 15:17:42
 */
import {
  chargeway,
  target,
  profess,
  creditflag,
  realnameflag,
  recordsType,
  gender,
  patstyle
} from "@/common/filter";

export const filter = {
  methods: {
    chargeway(type) {
      return chargeway(type);
    },
    target(type) {
      return target(type);
    },
    profess(type) {
      return profess(type);
    },
    creditflag(type) {
      return creditflag(type);
    },
    realnameflag(type) {
      return realnameflag(type);
    },
    recordsType(type) {
      return recordsType(type);
    },
    gender(type) {
      return gender(type);
    },
    patstyle(type) {
      return patstyle(type);
    }
  }
};
