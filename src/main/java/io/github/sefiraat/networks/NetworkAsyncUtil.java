package io.github.sefiraat.networks;

import com.google.common.base.Preconditions;
import io.github.sefiraat.networks.managers.ExperimentalFeatureManager;
import io.github.thebusybiscuit.slimefun4.core.debug.Debug;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.tasks.TickerTask;
import lombok.Getter;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class NetworkAsyncUtil {
    @Getter
    private static NetworkAsyncUtil instance;
    private Plugin plugin;
    public NetworkAsyncUtil() {
        instance = this;
    }
    private BukkitTask cleanTask ;
    public NetworkAsyncUtil init(Plugin plugin){
        this.plugin = plugin;
        Networks.getInstance().getLogger().info("Enabling Network Async Util");
        detect_async:
        {
            boolean isAsync;
            try{
                TickerTask ticker = Slimefun.getTickerTask();
                Class<?> tickerAsyncClass = ticker.getClass();
                Field asyncField = tickerAsyncClass.getDeclaredField("useAsync");
                asyncField.setAccessible(true);
                isAsync = (boolean) asyncField.get(ticker);
            }catch (Throwable e){
                isAsync = false;
            }
            if(isAsync){
                useAsync = true;
                Networks.getInstance().getLogger().info("Async Ticker Task Detected,Enabling Parallel Running Protector");
                //每5分钟刷新一次锁缓存,防止垃圾堆积
                cleanTask = (new BukkitRunnable() {
                    public void run() {
                        resetLockRecord();
                    }
                }).runTaskTimerAsynchronously(plugin, 5*60*20, 5*60*20);
            }else{
                useAsync = false;
                cleanTask = null;
            }

        }
        Preconditions.checkNotNull( getParallelExecutor());
        return this;
    }
    public void deconstruct() {
        if(cleanTask != null){
            cleanTask.cancel();
        }
        if(parallelExecutor != null){
            parallelExecutor.shutdown();
        }
        Networks.getInstance().getLogger().info("Disabling Network Async Util");
        return;
    }
    private AbstractExecutorService parallelExecutor;
    @Getter
    private boolean useAsync;
    private AbstractExecutorService genPool(){
        //do we really need this?
//        try{
//            if(useAsync){
//                Class<? extends TickerTask> asyncTickerTask = Slimefun.getTickerTask().getClass();
//                Field executorField = asyncTickerTask.getDeclaredField("tickerThreadPool");
//                executorField.setAccessible(true);
//                AbstractExecutorService service = (AbstractExecutorService) executorField.get(Slimefun.getTickerTask());
//                Preconditions.checkNotNull(service);
//                Networks.getInstance().getLogger().info("Using Slimefun TickerTask Executor Service as thread pool");
//                return service;
//            }
//        }catch (Throwable anything){
//
//        }
        int parallelism =Math.min((Runtime.getRuntime().availableProcessors()/2),8) ;
        ForkJoinPool executor= new ForkJoinPool(parallelism);
        Networks.getInstance().getLogger().info("Starting Thread Pool with parallelism "+parallelism);
//        Networks.getInstance().getLogger().info("Starting Thread Pool using coreSize "+parallelism+" and maxSize "+3*parallelism);
//                    new ThreadPoolExecutor(parallelism,3*parallelism,60, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(5000), new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;

    }
    private void restartPool(){
        if(parallelExecutor != null){
            Networks.getInstance().getLogger().info("Restarting Thread Pool");
            parallelExecutor.shutdown();
        }
        parallelExecutor = genPool();
    }
    private void resetLockRecord(){
        synchronized (locks){
            locks.clear();
        }
    }
    private final Map<Location,ReentrantLock> locks = new ConcurrentHashMap<>();

    public void ensureLocation(Location location,Runnable runnable){
        if(useAsync||ExperimentalFeatureManager.getInstance().isEnableNotWaitTillJoin()){
            var lock = getLocationLock(location);
            lock.lock();
            try{
                runnable.run();
            }finally {
                lock.unlock();
            }
        }else {
            runnable.run();
        }
    }
    public <T extends Object> T ensureLock(ReentrantLock lock, Supplier<T> runnable){
        if(useAsync||ExperimentalFeatureManager.getInstance().isEnableNotWaitTillJoin()){
            lock.lock();
            try{
                return runnable.get();
            }finally {
                lock.unlock();
            }
        }else {
            return runnable.get();
        }
    }
    @Nonnull
    public ReentrantLock getLocationLock(Location location){
        synchronized (locks){
            return locks.computeIfAbsent(location, k -> new ReentrantLock());
        }
    }


    public void runParallel(){

    }
    public AbstractExecutorService getParallelExecutor(){
        if(parallelExecutor == null || parallelExecutor.isShutdown()){
            restartPool();
        }
        return parallelExecutor;
    }
}
