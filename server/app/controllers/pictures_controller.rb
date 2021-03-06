class PicturesController < ApplicationController
  requires_session
  requires_playing

  def create
    FileUtils.mkpath(current_game.path)
    filename = "#{current_user.id}_#{Time.now.strftime('%Y%m%d_%H%M%S.jpg')}"
    fullpath = File.join(current_game.path, filename)

    File.open(fullpath, 'wb') { |f| f.write(params[:file].read) }

    GameChannel.broadcast_to current_user.game,
      type: 'added_picture',
      who: current_user.phone_number,
      path: filename

    head :created
  end

  def show
    fullpath = File.join(current_game.path, "#{params[:id]}.#{params[:format]}")
    return head :not_found if not File.exists?(fullpath)
    data = File.open(fullpath, 'rb') { |f| f.read }

    send_data data, type: 'image/jpeg', disposition: 'inline'
  end
end
