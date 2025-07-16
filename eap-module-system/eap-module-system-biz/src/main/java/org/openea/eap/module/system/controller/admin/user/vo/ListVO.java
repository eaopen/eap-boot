package org.openea.eap.module.system.controller.admin.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListVO<T> {

    private List<T>  list;

}
