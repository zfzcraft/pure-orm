package cn.zfz.pureorm.crud.update;

import cn.zfz.pureorm.enums.UpdateType;
import lombok.Data;

//更新节点（专门存更新相关）
@Data
public class UpdateNode {
  

  private final UpdateType type;
  private String field;
  private Object value;
  private String nativeSql;

  // SET / INCR / DECR
  public UpdateNode(UpdateType type, String field, Object value) {
      this.type = type;
      this.field = field;
      this.value = value;
  }

  // NATIVE
  public UpdateNode(UpdateType type, String nativeSql) {
      this.type = type;
      this.nativeSql = nativeSql;
  }

}
