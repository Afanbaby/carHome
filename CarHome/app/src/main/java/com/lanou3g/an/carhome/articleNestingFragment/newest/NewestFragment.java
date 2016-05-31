package com.lanou3g.an.carhome.articleNestingFragment.newest;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lanou3g.an.carhome.Collection;
import com.lanou3g.an.carhome.R;
import com.lanou3g.an.carhome.article.WebViewActivity;
import com.lanou3g.an.carhome.beas.BaseFragment;
import com.lanou3g.an.carhome.utils.VolleySinge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import it.sephiroth.android.library.picasso.Picasso;


/**
 * Created by anfeng on 16/5/9.
 * 推荐中的最新
 */
public class NewestFragment extends BaseFragment implements View.OnClickListener {

    private ListView listView;
    private PullToRefreshListView pullToRefreshListView;
    private NewestAdapter newestAdapter;
    private LayoutInflater inflater;
    private ViewPager mviewPager;
    /**
     * 用于小圆点图片
     */
    private List<ImageView> dotViewList;
    /**
     * 用于存放轮播效果图片
     */
    private List<ImageView> list;
    LinearLayout dotLayout;
    private int currentItem = 0;//当前页面
    boolean isAutoPlay = true;//是否自动轮播
    private ScheduledExecutorService scheduledExecutorService;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == 100) {
                mviewPager.setCurrentItem(currentItem);
            }
        }
    };
    private NewestBean newestBean;
    private ILoadingLayout startLabels;

    @Override
    public int setLayout() {
        return R.layout.fragment_newest;
    }

    @Override
    protected void initView() {
        pullToRefreshListView = bindView(R.id.item_newest_bom_lv);
        //设置下拉
        pullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        listView = pullToRefreshListView.getRefreshableView();

        newestAdapter = new NewestAdapter(context);
        View headView = getLayoutInflater(null).inflate(R.layout.image_item, null);
        listView.addHeaderView(headView);
        inflater = LayoutInflater.from(context);

        mviewPager = bindView(R.id.myviewPager);
        dotLayout = bindView(R.id.dotLayout);

        /******/
        dotLayout.removeAllViews();

        //判断是否轮播
        if (isAutoPlay) {
            //如果是，就开启轮播切换
            startPlay();
        }

    }

    @Override
    protected void initData() {
        startLabels = pullToRefreshListView.getLoadingLayoutProxy(true, false);
        startLabels.setRefreshingLabel("正在刷新");
        startLabels.setReleaseLabel("释放开始刷新");

        ILoadingLayout startLabelsNext = pullToRefreshListView.getLoadingLayoutProxy(false, true);
        startLabelsNext.setRefreshingLabel("正在加载");
        startLabelsNext.setPullLabel("上拉加载更多");

        VolleySinge.addRequest("http://app.api.autohome.com.cn/autov4.2.5/news/newslist-a2-pm1-v4.2.5-c0-nt0-p1-s30-l0.html",
                NewestBean.class,
                new Response.Listener<NewestBean>() {
                    @Override
                    public void onResponse(NewestBean response) {
                        newestBean = response;
                        //初始化小圆点
                        initViewImage();
                        newestAdapter.setNewestBean(newestBean);
                        pullToRefreshListView.setAdapter(newestAdapter);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });


        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                //下拉刷新
                VolleySinge.addRequest("http://app.api.autohome.com.cn/autov4.2.5/news/newslist-a2-pm1-v4.2.5-c0-nt0-p1-s30-l0.html",
                        NewestBean.class,
                        new Response.Listener<NewestBean>() {
                            @Override
                            public void onResponse(NewestBean response) {
                                newestBean = response;
                                newestAdapter.setNewestBean(newestBean);
                                pullToRefreshListView.onRefreshComplete();
                                String str = DateUtils.formatDateTime(getContext(), System.currentTimeMillis(), DateUtils.
                                        FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                                startLabels.setLastUpdatedLabel("最后更新时间:" + str);
                                Toast.makeText(context, "刷新数据成功", Toast.LENGTH_SHORT).show();
                                //轮播图的刷新
                                dotLayout.removeAllViews();
                                initViewImage();
//                                CustomProgressDialog dialog =new CustomProgressDialog(context, "正在加载中",R.anim.frame);
//                                dialog.show();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(context, "数据解析失败,请下拉刷新...", Toast.LENGTH_SHORT).show();
                            }
                        });


            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                //上拉加载
                VolleySinge.addRequest("http://app.api.autohome.com.cn/autov4.2.5/news/newslist-a2-pm1-v4.2.5-c0-nt0-p" + newestBean.getResult().getNewslist().get(newestBean.getResult().getNewslist().size() - 1).getId() + "-s30-l" + newestBean.getResult().getNewslist().get(newestBean.getResult().getNewslist().size() - 1).getLasttime() + ".html",
                        NewestBean.class,
                        new Response.Listener<NewestBean>() {
                            @Override
                            public void onResponse(NewestBean response) {
                                newestBean.getResult().getNewslist().addAll(response.getResult().getNewslist());
                                newestAdapter.setNewestBean(newestBean);
                                pullToRefreshListView.onRefreshComplete();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(context, "数据解析失败,请上拉加载...", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        pullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //当点击每一行的时候,将网站传过去
                Intent intent = new Intent();
                String url = "";
                int viewType = newestAdapter.getItemViewType(position - 2);
                switch (viewType) {
                    case 1:
                        url = "http://cont.app.autohome.com.cn/autov5.0.0/content/news/newscontent-n" +
                                newestBean.getResult().getNewslist().get(position - 2).getId() + "-t0.json";
                        break;
                    case 3://视频
                        url = "http://v.autohome.com.cn/v_4_" + newestBean.getResult().getNewslist().get(position - 2).getId() + ".html";
                        break;
                    case 5://热帖
                        url = "http://forum.app.autohome.com.cn/autov5.0.0/forum/club/topiccontent-a2-pm2-v5.0.0-t" + newestBean.getResult().getNewslist().get(position - 2).getId() + "-o0-p1-s20-c1-nt0-fs0-sp0-al0-cw320.json";
                        break;
                    default:
                        url = "http://cont.app.autohome.com.cn/autov4.2.5/content/News/newscontent-a2-pm1-v4.2.5-n" +
                                newestBean.getResult().getNewslist().get(position - 2).getId() + "-lz0-sp0-nt0-sa1-p0-c1-fs0-cw320.html";
                        break;
                }
                intent.putExtra("url", url);
                intent.setClass(context, WebViewActivity.class);
                //将详情中的内部类的newslistBean传过去
                int cId = newestBean.getResult().getNewslist().get(position - 2).getId();
                String cTitle = newestBean.getResult().getNewslist().get(position - 2).getTitle();
                String cUrl = url;
                String cSmallpic = newestBean.getResult().getNewslist().get(position - 2).getSmallpic();
                Collection collection = new Collection();
                collection.setId((long) cId);
                collection.setTitle(cTitle);
                collection.setUrl(cUrl);
                collection.setImageUrl(cSmallpic);
                intent.putExtra("Collection", collection);
                startActivity(intent);
            }
        });
    }


    public void initViewImage() {
        dotViewList = new ArrayList<ImageView>();
        list = new ArrayList<ImageView>();

        for (int i = 0; i < newestBean.getResult().getFocusimg().size(); i++) {
            ImageView dotView = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            params.leftMargin = 15;//设置小圆点的外边距
            params.rightMargin = 15;

            params.height = 20;//设置小圆点的大小
            params.width = 20;

            if (i == 0) {
                dotView.setBackgroundResource(R.mipmap.point_pressed);
            } else {

                dotView.setBackgroundResource(R.mipmap.point_unpressed);
            }
            dotLayout.addView(dotView, params);

            dotViewList.add(dotView);
            //上面是动态添加了四个小圆点
        }

        //设置轮播图的图片
        for (int i = 0; i < newestBean.getResult().getFocusimg().size(); i++) {
            String url = newestBean.getResult().getFocusimg().get(i).getImgurl();
            ImageView img = (ImageView) inflater.inflate(R.layout.scroll_vew_item, null);
            img.setOnClickListener(this);
            Picasso.with(context).load(url).placeholder(R.mipmap.fild).error(R.mipmap.fild).into(img);
            list.add(img);
        }

        ImagePaperAdapter adapter = new ImagePaperAdapter((ArrayList<ImageView>) list);
        mviewPager.setAdapter(adapter);
        mviewPager.setCurrentItem(0);
        mviewPager.setOnPageChangeListener(new MyPageChangeListener());
    }

    /**
     * 开始轮播图切换
     */
    private void startPlay() {
        //ExecutorService：可安排在给定的延迟后运行或定期执行的命令。
        //scheduleAtFixedRate 和 scheduleWithFixedDelay 方法创建并执行某些在取消前一直定期运行的任务。
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new SlideShowTask(), 1, 4, TimeUnit.SECONDS);
        //根据他的参数说明，第一个参数是执行的任务(这里就是切换轮播图的任务)，第二个参数是第一次执行的间隔，第三个参数是执行任务的周期；
    }

    //轮播图的点击事件
    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        String url = "http://v.autohome.com.cn/Error/_404?aspxerrorpath=/v_4_.html";
        intent.putExtra("url", url);
        intent.setClass(context, WebViewActivity.class);
        startActivity(intent);
    }

    /**
     * 执行轮播图切换任务
     */
    private class SlideShowTask implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            synchronized (mviewPager) {
                currentItem = (currentItem + 1) % list.size();
                handler.sendEmptyMessage(100);
            }
        }
    }

    /**
     * ViewPager的监听器
     * 当ViewPager中页面的状态发生改变时调用
     */
    private class MyPageChangeListener implements ViewPager.OnPageChangeListener {
        //当用手滑动的时候，将轮播取消
        boolean isAutoPlay = false;

        //onPageScrollStateChanged：当状态发生改变的时候回调用
        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub
            switch (arg0) {
                case 1:// 手势滑动，空闲中；arg0 = 1 默示正在滑动
                    isAutoPlay = false;
                    System.out.println(" 手势滑动，空闲中");
                    break;
                case 2:// 界面切换中;arg0==2的时辰默示滑动完毕了
                    isAutoPlay = true;
                    System.out.println(" 界面切换中");
                    break;
                case 0:// 滑动结束，即切换完毕或者加载完毕;arg0==0的时辰默示什么都没做
                    // 当前为最后一张，此时从右向左滑，则切换到第一张
                    if (mviewPager.getCurrentItem() == mviewPager.getAdapter().getCount() - 1 && !isAutoPlay) {
                        mviewPager.setCurrentItem(0);
                        System.out.println(" 滑动到最后一张");
                    }
                    // 当前为第一张，此时从左向右滑，则切换到最后一张
                    else if (mviewPager.getCurrentItem() == 0 && !isAutoPlay) {
                        mviewPager.setCurrentItem(mviewPager.getAdapter().getCount() - 1);
                        System.out.println(" 滑动到第一张");
                    }
                    break;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub
        }

        //当页面改变的时候调用的方法，也就是滑动页面的时候，改变小圆点的背景颜色
        @Override
        public void onPageSelected(int pos) {
            // TODO Auto-generated method stub
            //这里面动态改变小圆点的被背景，来实现效果
            currentItem = pos;
            for (int i = 0; i < dotViewList.size(); i++) {
                if (i == pos) {
                    ((View) dotViewList.get(pos)).setBackgroundResource(R.mipmap.point_pressed);
                } else {
                    ((View) dotViewList.get(i)).setBackgroundResource(R.mipmap.point_unpressed);
                }
            }
        }

    }
}
