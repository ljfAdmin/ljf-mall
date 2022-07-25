package com.ljf.thread;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executors;

/**
 * Java使用 Delayed 实现延迟任务
 * 延迟任务队列
 * */
@Component
public class TaskService {
    /**
     * 注：
     *      DelayQueue是一个带有延迟时间的无界阻塞队列。队列中的元素，只有等延时时间到了，才能取出来。
     *  ！！！此队列一般用于过期数据的删除，或任务调度！！！。以下，模拟一下定长时间的数据删除。
     *
     *  特点
     * （1）无边界设计
     * （2）添加（put）不阻塞，移除阻塞
     * （3）元素都有一个过期时间
     * （4）取元素只有过期的才会被取出
     *
     *  DelayQueue是BlockingQueue的一种，所以它是线程安全的，
     *  DelayQueue的特点就是插入Queue中的数据可以按照自定义的delay时间进行排序。
     *  只有delay时间小于0的元素才能够被取出。
     *
     *  DelayQueue的定义：
     *      public class DelayQueue<E extends Delayed> extends AbstractQueue<E>
     *       implements BlockingQueue<E>
     *  从定义可以看到，DelayQueue中存入的对象都必须是Delayed的子类。
     *  Delayed继承自Comparable，并且需要实现一个getDelay的方法。
     *  为什么这样设计呢？
     *   因为DelayQueue的底层存储是一个PriorityQueue，PriorityQueue是一个可排序的Queue，
     *      其中的元素必须实现Comparable方法。
     *   而getDelay方法则用来判断排序后的元素是否可以从Queue中取出。
     *
     *   注：队列的头部，是延迟期满后保存时间最长的delay元素,是最接近过期的元素。没有过期元素的话，
     *  使用poll()方法会返回null值
     *
     *  部分讲解：https://blog.csdn.net/qq_41489540/article/details/116009078
     *
     * */
    private final DelayQueue<Task> delayQueue = new DelayQueue<>();

    /**
     * 注：
     *  @PostConstruct ：该注解被用来修饰一个非静态的void（）方法。
     *  被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，并且只会被服务器执行一次。
     * PostConstruct在构造函数之后执行，init（）方法之前执行。
     *
     *  通常我们会是在Spring框架中使用到@PostConstruct注解 该注解的方法在整个Bean初始化中的执行顺序：
     *      Constructor(构造方法) -> @Autowired(依赖注入) -> @PostConstruct(注释的方法)
     * */
    @PostConstruct
    private void init() {
        /**
         * 将一个线程加入到线程池中，执行，从延时队列中取出延时任务并执行其run方法进行回滚
         * */
        Executors.newSingleThreadExecutor().execute(() -> {
            while (true) {
                try {
                    Task task = delayQueue.take();
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 添加一个任务：
     *  如果存在，直接返回；
     *  如果不存在，直接add添加到延时任务队列末尾
     * */
    public void addTask(Task task) {
        if (delayQueue.contains(task)) {
            return;
        }
        delayQueue.add(task);
    }

    /**
     * 删除一个任务，但是只有延时时间到了才会被移除
     * */
    public void removeTask(Task task) {
        delayQueue.remove(task);
    }
}
