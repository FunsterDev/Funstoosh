class GamesController < ApplicationController
  requires_session

  def create
    @game = Game.create!(users: [current_user], invite: params[:phone_numbers].split(','))
    render json: { id: @game.id }, status: :created
  end
end
