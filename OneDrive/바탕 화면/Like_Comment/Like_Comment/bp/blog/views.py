from django.shortcuts import render,get_object_or_404,redirect
from .models import Blog,Comment,Like
from django.utils import timezone
# Create your views here.
def blog(request):
    blogs = Blog.objects.all()
    return render(request,'blog.html', { 'blogs' : blogs })

# R
def detail(request, blog_id):
    # 현재 게시글 한개를 가져옴.
    detail = get_object_or_404(Blog, pk=blog_id)
    comments = Comment.objects.filter( post = detail)

    #like_num = len([ _ for _ in Like.objects.filter(blog = detail)])
    #print(like_num)
    
    #현재 user가 좋아요를 눌렀을 때, 안눌렀을 때 다른 메세지를 보내야됨
    #likes => 순서쌍 , ('현재,blog_id' , ' user_id' )로 현재 user.id와 같은 data가 존재 한다면 => 이미 좋아요 누름.
    if detail.likes.filter(id = request.user.id):    
        message = "좋아요 취소"
    else:
        message = "좋아요"

    
    return render(request ,'detail.html', { 'detail' : detail,'comments':comments ,'message':message} )


def new(request):
    return render(request, 'new.html')

def create(request):
    blog = Blog() # 객체 틀 하나 가져오기
    blog.title = "NoTitle"  # 내용 채우기
    if request.GET['title']:
        blog.title=request.GET['title']
    blog.body = request.GET['body'] # 내용 채우기
    blog.pub_date = timezone.datetime.now() # 내용 채우기
    blog.writer = request.user
    blog.save() # 객체 저장하기

    # 새로운 글 url 주소로 이동
    return redirect('/blog/' + str(blog.id))

#삭제
def delete(request, blog_id):
    blog = get_object_or_404(Blog, pk=blog_id)
    blog.delete()
    return redirect('/blog/')
#update

def update(request, blog_id):
    blog = get_object_or_404(Blog, pk =blog_id)

    if request.method == "POST":
        if request.POST['title']:
            blog.title=request.POST['title']
        blog.body = request.POST['body']
        blog.pub_date = timezone.datetime.now()
        blog.save()
        return redirect('/blog/' +str(blog.id))
    else:
        return render(request,'update.html')



# Comment create
def comment(request,blog_id):
    if request.method == "POST":

        comment = Comment()
        comment.body = request.POST['body']
        comment.pub_date = timezone.datetime.now()
        comment.writer = request.user
        comment.post = get_object_or_404(Blog , pk=blog_id)
        comment.save()

        return redirect('/blog/'+str(blog_id))
    else:
        return redirect('/blog/'+str(blog_id))

# Comment delete
def comment_delete(request, comment_id):
    comment = get_object_or_404(Comment, pk=comment_id)
    blog_id = comment.post.id
    comment.delete()

    return redirect('/blog/'+str(blog_id))


def post_like(request,blog_id):
    blog = get_object_or_404(Blog ,pk = blog_id)
    user = request.user
    #이미 좋아요를 눌른 상태라면 like모델을 삭제하여, 좋아요 취소 상태로 만듬
    if blog.likes.filter(id=user.id):
        blog.likes.remove(user)         #현재 유저가 있는 ('현재 blog_id' , '현재 user' ) like모델을 삭제함.
    else:
        blog.likes.add(user)            #추가

    return redirect('/blog/'+str(blog_id))