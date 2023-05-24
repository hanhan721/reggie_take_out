package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /*
     * 新增分类
     * */
    @PostMapping
    public R<String> add(@RequestBody Category category) {
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /*
     * 分页查询展示
     * */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        Page<Category> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.orderByAsc(Category::getSort);
        categoryService.page(pageInfo, lqw);
        return R.success(pageInfo);
    }

    /*
     * 删除
     * */
    @DeleteMapping
    public R<String> delete(long ids) {
        categoryService.remove(ids);
        return R.success("删除成功");
    }

    /*
     * 编辑
     * */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info(category.toString());  //日志调试
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    /*
     * 新增菜品时,菜品分类数据回显
     * */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.eq(category.getType() != null, Category::getType, category.getType());
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(lqw);
        return R.success(list);
    }
}
